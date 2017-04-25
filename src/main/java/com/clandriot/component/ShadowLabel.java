package com.clandriot.component;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ShadowLabel extends JLabel {
  int shiftX;
  int shiftY;
  int alpha;

  public ShadowLabel(String fontName, int fontSize, Color foreground, int alpha,  int shiftX, int shiftY) {
    super();

    this.shiftX = shiftX;
    this.shiftY = shiftY;

    this.setFont(new Font(fontName,Font.BOLD, fontSize));
    setStyle(foreground, alpha);
  }

  public void setStyle(Color foreground, int alpha) {
    this.setForeground(foreground);
    this.alpha = alpha;
  }

  public Dimension getPreferredSize() {
    String text = getText();
    FontMetrics fm = this.getFontMetrics(getFont());

    int w = fm.stringWidth(text);
    w += this.shiftX;

    int h = fm.getHeight();
    h += this.shiftY;

    return new Dimension(w,h);
  }

  public void paintComponent(Graphics g) {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    char[] chars = getText().toCharArray();

    FontMetrics fm = this.getFontMetrics(getFont());
    int h = fm.getAscent();
    LineMetrics lm = fm.getLineMetrics(getText(),g);
    g.setFont(getFont());

    int x = 0;

    if (this.getHorizontalAlignment() == JLabel.CENTER) {
      Rectangle rect = this.getBounds(null);
      x = (int)(rect.getWidth() / 2) - (int)(this.getPreferredSize().getWidth() / 2);
    }

    for(int i=0; i<chars.length; i++) {
      char ch = chars[i];
      int w = fm.charWidth(ch);

      g.setColor(new Color(64,64,64,alpha));
      g.drawString(""+chars[i],x+this.shiftX,h+this.shiftY);

      g.setColor(getForeground());
      g.drawString(""+chars[i],x,h);

      x+=w;
    }
  }
}
