package com.clandriot.exchange;

import java.util.*;
import java.util.concurrent.*;
import java.lang.*;

import javax.mail.*;
import javax.mail.event.*;

import com.sun.mail.imap.*;

import com.clandriot.exchange.events.*;

public class MailScan implements MessageCountListener {
  private String host = "outlook.office365.com";
  private String user = System.getenv("MAIL_USER");
  private String password = System.getenv("MAIL_PWD");
  private Store store = null;
  private Session session = null;
  private IdleManager idleManager = null;
	private ExecutorService es = Executors.newCachedThreadPool();
  private List<MessageCountUpdateListener> listeners = new ArrayList<MessageCountUpdateListener>();

  public MailScan() throws Exception {
    startScan();
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
  public void messagesAdded(MessageCountEvent evt) {
    Folder folder = (Folder)evt.getSource();
    for (MessageCountUpdateListener lstn : listeners) {
      lstn.messageAdded();
    }
    try {
      idleManager.watch(folder);
    }
    catch (MessagingException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void messagesRemoved(MessageCountEvent evt) {
    Folder folder = (Folder)evt.getSource();
    for (MessageCountUpdateListener lstn : listeners) {
      lstn.messageRemoved();
    }
    try {
      idleManager.watch(folder);
    }
    catch (MessagingException ex) {
      ex.printStackTrace();
    }
  }

  private void startScan() throws Exception {
    try {
      Folder inbox = getStore().getFolder("Inbox");
      inbox.open(Folder.READ_WRITE);
      this.idleManager = new IdleManager(getSession(), es);
      inbox.addMessageCountListener(this);
      idleManager.watch(inbox);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new Exception(ex);
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
