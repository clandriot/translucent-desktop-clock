package com.clandriot.exchange.events;

public interface MessageCountUpdateListener {
  public void messageAdded(int nbUnreadMessageCount);
  public void messageRemoved(int nbUnreadMessageCount);
}
