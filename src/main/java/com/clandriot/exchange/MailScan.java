package com.clandriot.exchange;

import java.util.*;
import java.util.concurrent.*;
import java.lang.*;

import java.awt.event.*;

import javax.mail.*;
import javax.mail.event.*;
import javax.swing.Timer;

import com.sun.mail.imap.*;

import com.clandriot.exchange.events.*;
import com.clandriot.exchange.exception.*;

public class MailScan implements MessageCountListener, Runnable {
  private String host = "outlook.office365.com";
  private String user = System.getenv("MAIL_USER");
  private String password = System.getenv("MAIL_PWD");
  private Store store = null;
  private Session session = null;
  private IdleManager idleManager = null;
	private ExecutorService es = Executors.newCachedThreadPool();
  private List<MessageCountUpdateListener> listeners = new ArrayList<MessageCountUpdateListener>();
  private ScheduledExecutorService keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();

  public MailScan() {
  }

  public int getUnreadMessageCount() throws NoSuchProviderException, AuthenticationFailedException, MessagingException, IllegalStateException, FolderNotFoundException {
    int unreadMsg = 0;

    Folder inbox = getStore().getFolder("Inbox");
		inbox.open(Folder.READ_ONLY);
	  unreadMsg = inbox.getUnreadMessageCount();

    return unreadMsg;
  }

  public void addMessageCountUpdateListener(MessageCountUpdateListener lstn) {
    listeners.add(lstn);
  }

  @Override
  public void run() {
    try {
      startScan();
    }
    catch (MailScanException ex) {
      ex.printStackTrace();
    }
    checkInterrupt(Thread.currentThread());
  }

  @Override
  public void messagesAdded(MessageCountEvent evt) {
    try {
      Folder folder = (Folder)evt.getSource();
      for (MessageCountUpdateListener lstn : listeners) {
        lstn.messageAdded(folder.getUnreadMessageCount());
      }
      checkFolder(folder);
      idleManager.watch(folder);
    }
    catch (MessagingException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void messagesRemoved(MessageCountEvent evt) {
    try {
      Folder folder = (Folder)evt.getSource();
      for (MessageCountUpdateListener lstn : listeners) {
        lstn.messageRemoved(getUnreadMessageCount());
      }
      checkFolder(folder);
      idleManager.watch(folder);
    }
    catch (MessagingException ex) {
      ex.printStackTrace();
    }
  }

  private void startScan() throws MailScanException {
    try {
      Folder inbox = getStore().getFolder("Inbox");
      inbox.open(Folder.READ_ONLY);
      this.idleManager = new IdleManager(getSession(), es);
      inbox.addMessageCountListener(this);
      keepAlive(inbox, this.idleManager);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new MailScanException(ex);
    }
  }

  private void keepAlive(Folder folder, IdleManager idle) {
    Runnable keepAlive = new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println("Refreshing idle manager");
          checkFolder(folder);
          idle.watch(folder);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    keepAliveExecutor.scheduleAtFixedRate(keepAlive, 0, 1, TimeUnit.MINUTES);
  }

  private void stopScan() {
    idleManager.stop();
    es.shutdownNow();
    keepAliveExecutor.shutdownNow();
  }

  private void checkInterrupt(Thread th) {
    while (true) {
      try {
        Thread.sleep(500);
      }
      catch (InterruptedException ex) {
          stopScan();
          return;
      }
    }
  }

  private void checkFolder(Folder folder) {
    if (!folder.isOpen()) {
      System.out.println("Folder closed, reopening it");
      try {
        folder.open(Folder.READ_ONLY);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private final Session getSession() {
    if (this.session == null) {
      Properties props = new Properties();
      props.setProperty("mail.imaps.usesocketchannels", "true");

      this.session = Session.getInstance(props);
    }

    return this.session;
  }

  private Store getStore() throws NoSuchProviderException, AuthenticationFailedException, MessagingException, IllegalStateException {
    if (store == null) {
        store = getSession().getStore("imaps");
        store.connect(host, user, password);
    } else if (!store.isConnected()) {
      store.connect(host, user, password);
    }

    return store;
  }
}
