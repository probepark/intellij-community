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
package com.intellij.ide.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.ui.components.JBRadioButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Dmitry Avdeev
 */
public class ChooseTemplateStep extends ModuleWizardStep {

  private final WizardContext myWizardContext;
  private final ProjectTypeStep myProjectTypeStep;

  private JPanel myPanel;
  private JBRadioButton myEmptyProjectButton;
  private JBRadioButton myFromTemplateButton;
  private ProjectTemplateList myTemplateList;

  public ChooseTemplateStep(WizardContext wizardContext, ProjectTypeStep projectTypeStep) {
    myWizardContext = wizardContext;
    myProjectTypeStep = projectTypeStep;
    ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateSelection();
      }
    };
    myEmptyProjectButton.addActionListener(listener);
    myFromTemplateButton.addActionListener(listener);
    updateSelection();
  }

  private void updateSelection() {
    myTemplateList.setEnabled(myFromTemplateButton.isSelected());
  }

  @Override
  public boolean isStepVisible() {
    return myWizardContext.isCreatingNewProject() && !myProjectTypeStep.getAvailableTemplates().isEmpty();
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void updateStep() {
    myTemplateList.setTemplates(new ArrayList<ProjectTemplate>(myProjectTypeStep.getAvailableTemplates()), false);
  }

  @Override
  public void updateDataModel() {
    if (myFromTemplateButton.isSelected()) {
      myWizardContext.setProjectTemplate(myTemplateList.getSelectedTemplate());
    }
  }
}
