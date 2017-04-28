package com.clandriot.exchange;

import java.util.*;
import java.lang.*;

import javax.mail.*;
import javax.mail.event.*;

public class MailScan {
  private String host = "outlook.office365.com";
  private String user = System.getenv("MAIL_USER");
  private String password = System.getenv("MAIL_PWD");
  private Store store = null;
  private Session session = Session.getInstance(new Properties());

  public MailScan() {
  }

  public int getUnreadMessageCount() {
    int unreadMsg = -1;
    try {
      Folder inbox = getStore().getFolder("Inbox");
  		inbox.open(Folder.READ_WRITE);
		  unreadMsg = inbox.getUnreadMessageCount();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    return unreadMsg;
  }

  private Store getStore() {
    if (store == null) {
      try {
        store = this.session.getStore("imaps");
        store.connect(host, user, password);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    } else if (!store.isConnected()) {
      try {
        store.connect(host, user, password);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return store;
  }
}
