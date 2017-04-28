package com.clandriot;

import com.clandriot.component.*;
import com.clandriot.exchange.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.*;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class Clock {

  static Point screen0 = new Point(3,987);

  int posX=0,posY=0;
  Color defaultForeground = new Color(255,255,255,166);
  Color foreground = defaultForeground;
  int alpha = defaultForeground.getAlpha();
  ShadowLabel localTime = new ShadowLabel("Petitinho", 30, foreground, alpha, 2, 3);
  ShadowLabel day = new ShadowLabel("Calibri",14, foreground, alpha, 1, 1);
  ShadowLabel date = new ShadowLabel("Calibri",14, foreground, alpha, 1, 1);
  ShadowLabel msg = new ShadowLabel("Calibri",14, foreground, alpha, 1, 1);
  ShadowLabel foreignTime = new ShadowLabel("Petitinho", 30, foreground, alpha, 2, 3);

  public static void main(String[] args) {
    MailScan mailscan = new MailScan();
    System.out.println("Unread msgs = " + mailscan.getUnreadMessageCount());
    //new Clock();
  }

  public Clock() {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        final JWindow frame = new JWindow();
        frame.setAlwaysOnTop(true);
        frame.setBackground(new Color(0,0,0,0));

        TranslucentPane pane = new TranslucentPane();
        pane.setLayout(new BorderLayout());
        frame.setContentPane(pane);

        localTime.setText(getTime());
        day.setText(getDay());
        date.setText(getDate());
        foreignTime.setText(getForeignTime());
        msg.setHorizontalAlignment(JLabel.CENTER);
        msg.setText("for later...");

        pane.add(localTime,BorderLayout.PAGE_START);
        pane.add(day,BorderLayout.LINE_START);
        pane.add(msg, BorderLayout.CENTER);
        pane.add(date,BorderLayout.LINE_END);
        pane.add(foreignTime,BorderLayout.PAGE_END);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setLocation(screen0);
        frame.setVisible(true);

        frame.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1 &&  e.getButton() == MouseEvent.BUTTON2) {
              moveFrame( frame );
            }
            else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
              SwingUtilities.getWindowAncestor(e.getComponent()).dispose();
            }
          }
          @Override
          public void mousePressed(MouseEvent e) {
            posX=e.getX();
            posY=e.getY();
          }
        });
        frame.addMouseMotionListener(new MouseAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            frame.setLocation (e.getXOnScreen()-posX,e.getYOnScreen()-posY);
          }
        });

        ActionListener refresh = new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            localTime.setText(getTime());
            foreignTime.setText(getForeignTime());
            day.setText(getDay());
            date.setText(getDate());
            frame.repaint();
          }
        };
        new Timer(1000,refresh).start();
      }
    });
  }

	public void moveFrame(JWindow frame) {
		frame.setLocation(screen0);
	}

	public String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(cal.getTime());
	}

	public String getForeignTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("America/Phoenix"));
		return sdf.format(cal.getTime());
	}

	public String getDay() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("E");
		return sdf.format(cal.getTime());
	}

	public String getDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
		return sdf.format(cal.getTime());
	}
}
