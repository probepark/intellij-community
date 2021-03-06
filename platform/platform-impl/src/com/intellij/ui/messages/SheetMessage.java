/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ui.messages;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


/**
 * Created by Denis Fokin
 */
public class SheetMessage  implements ActionListener {
  private JDialog myWindow;
  private Window myParent;
  private SheetController myController;
  private Timer myAnimator = new Timer(5,this);

  private boolean myShouldEnlarge = true;

  private final static int SHEET_ANIMATION_STEP = 1;

  private Image staticImage;
  private int imageHeight;

  public SheetMessage(final Window owner,
                      final String title,
                      final String message,
                      final Icon icon,
                      final String[] buttons,
                      final DialogWrapper.DoNotAskOption doNotAskOption,
                      final String focusedButton,
                      final String defaultButton)
  {
    myWindow = new JDialog(owner, "This should not be shown", Dialog.ModalityType.APPLICATION_MODAL);
    myAnimator.setInitialDelay(0);


    myParent = owner;
    myWindow.setSize(SheetController.SHEET_WIDTH, SheetController.SHEET_HEIGHT);
    myWindow.setUndecorated(true);
    myWindow.setBackground(new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)));
    myController = new SheetController(this, title, message, icon, buttons, defaultButton, doNotAskOption, focusedButton);


    imageHeight = 0;
    registerMoveResizeHandler();
    myWindow.setFocusableWindowState(true);
    myWindow.setFocusable(true);

    startAnimation();
    myWindow.setVisible(true);
    setPositionRelativeToParent();
  }

  public boolean toBeShown() {
    return !myController.getDoNotAskResult();
  }

  public String getResult() {
    return myController.getResult();
  }

  void startAnimation () {
    staticImage = myController.getStaticImage();
    JPanel staticPanel = new JPanel() {
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        if (staticImage != null) {
          Graphics2D g2d = (Graphics2D) g.create();

          g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));

          int imageCropOffset = (UIUtil.isRetina()) ? imageHeight * 2 : imageHeight;

          g.drawImage(staticImage, 0,0,SheetController.SHEET_WIDTH,imageHeight,
                      0, staticImage.getHeight(null) - imageCropOffset,
                      staticImage.getWidth(null) ,staticImage.getHeight(null) ,null);
        }
      }
    };
    staticPanel.setOpaque(false);
    staticPanel.setSize(SheetController.SHEET_WIDTH,SheetController.SHEET_HEIGHT);
    myWindow.setContentPane(staticPanel);
    myAnimator.start();
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    imageHeight = (myShouldEnlarge) ? imageHeight + SHEET_ANIMATION_STEP
                                         : imageHeight - SHEET_ANIMATION_STEP;

    setPositionRelativeToParent();
    if (imageHeight > SheetController.SHEET_HEIGHT) {
      myAnimator.stop();
      imageHeight = SheetController.SHEET_HEIGHT;
      staticImage = null;
      myWindow.setContentPane(myController.getPanel(myWindow));
      myController.requestFocus();
      myShouldEnlarge = false;
    }

    if (imageHeight < 0) {
      myAnimator.stop();
      myWindow.dispose();
    }

    myWindow.repaint();
  }

  private void setPositionRelativeToParent () {
    int width = myParent.getWidth();
    myWindow.setLocation(width / 2 - SheetController.SHEET_WIDTH / 2 + myParent.getLocation().x,
                         myParent.getInsets().top + myParent.getLocation().y);
  }

  private void registerMoveResizeHandler () {
    myParent.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        super.componentResized(e);
        setPositionRelativeToParent();
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        super.componentMoved(e);
        setPositionRelativeToParent();
      }
    });
  }
}



