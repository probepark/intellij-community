/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.jetbrains.python.sdk;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.NullableConsumer;
import com.jetbrains.python.remote.PythonRemoteInterpreterManager;
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PythonSdkDetailsStep extends BaseListPopupStep<String> {
  private static DialogWrapper myMore;
  private final Project myProject;
  private final Component myOwnerComponent;
  private final Sdk[] myExistingSdks;
  private final NullableConsumer<Sdk> myCallback;

  private static final String LOCAL = "Add Local";
  private static final String REMOTE = "Add Remote";
  private static final String VIRTUALENV = "Create VirtualEnv";
  private static final String MORE = "More...";

  public static void show(final Project project,
                          final Sdk[] existingSdks,
                          DialogWrapper moreDialog,
                          JComponent ownerComponent, final RelativePoint popupPoint,
                          final boolean showMore,
                          final NullableConsumer<Sdk> callback) {
    myMore = moreDialog;
    final ListPopupStep sdkHomesStep = new PythonSdkDetailsStep(project, ownerComponent, existingSdks, showMore, callback);
    final ListPopup popup = JBPopupFactory.getInstance().createListPopup(sdkHomesStep);
    popup.show(popupPoint);
  }

  public PythonSdkDetailsStep(Project project,
                              Component ownerComponent,
                              Sdk[] existingSdks,
                              boolean showMore,
                              NullableConsumer<Sdk> callback) {
    super(null, getAvailableOptions(showMore));
    myProject = project;
    myOwnerComponent = ownerComponent;
    myExistingSdks = existingSdks;
    myCallback = callback;
  }

  private static List<String> getAvailableOptions(boolean showMore) {
    final List<String> options = new ArrayList<String>();
    options.add(LOCAL);
    if (PythonRemoteInterpreterManager.getInstance() != null) {
      options.add(REMOTE);
    }
    options.add(VIRTUALENV);

    if (showMore) {
      options.add(MORE);
    }
    return options;
  }

  @Nullable
  @Override
  public ListSeparator getSeparatorAbove(String value) {
    return MORE.equals(value) ? new ListSeparator() : null;
  }

  private void optionSelected(final String selectedValue) {
    if (LOCAL.equals(selectedValue)) {
      createLocalSdk();
    }
    else if (REMOTE.equals(selectedValue)) {
      createRemoteSdk();
    }
    else if (VIRTUALENV.equals(selectedValue)) {
      createVirtualEnvSdk();
    }
    else {
      myMore.show();
    }
  }

  private void createLocalSdk() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        SdkConfigurationUtil.createSdk(myProject, myExistingSdks, myCallback, false, PythonSdkType.getInstance());
      }
    }, ModalityState.any());
  }

  private void createRemoteSdk() {
    PythonRemoteInterpreterManager remoteInterpreterManager = PythonRemoteInterpreterManager.getInstance();
    if (remoteInterpreterManager != null) {
      remoteInterpreterManager.addRemoteSdk(myProject, myOwnerComponent, Lists.newArrayList(myExistingSdks), myCallback);
    }
    else {
      Messages.showErrorDialog("The Remote Hosts Access plugin is missing. Please enable the plugin in " +
                               ShowSettingsUtil.getSettingsMenuName() +
                               " | Plugins.", "Add Remote Interpreter");
    }
  }

  private void createVirtualEnvSdk() {
    CreateVirtualEnvDialog.VirtualEnvCallback callback = new CreateVirtualEnvDialog.VirtualEnvCallback() {
      @Override
      public void virtualEnvCreated(Sdk sdk, boolean associateWithProject) {
        PythonSdkType.setupSdkPaths(sdk, myProject, null);
        if (associateWithProject) {
          SdkAdditionalData additionalData = sdk.getSdkAdditionalData();
          if (additionalData == null) {
            additionalData = new PythonSdkAdditionalData(PythonSdkFlavor.getFlavor(sdk.getHomePath()));
            ((ProjectJdkImpl)sdk).setSdkAdditionalData(additionalData);
          }
          ((PythonSdkAdditionalData)additionalData).associateWithProject(myProject);
        }
        myCallback.consume(sdk);
      }
    };

    final CreateVirtualEnvDialog dialog;
    final List<Sdk> allSdks = Lists.newArrayList(myExistingSdks);

    final List<PythonSdkFlavor> flavors = PythonSdkFlavor.getApplicableFlavors(false);
    for (PythonSdkFlavor flavor : flavors) {
      final Collection<String> strings = flavor.suggestHomePaths();
      for (String string : strings) {
        allSdks.add(new PyDetectedSdk(string));
      }
    }

    if (myProject != null) {
      dialog = new CreateVirtualEnvDialog(myProject, allSdks, null);
    }
    else {
      dialog = new CreateVirtualEnvDialog(myOwnerComponent, allSdks, null);
    }
    dialog.show();
    if (dialog.isOK()) {
      dialog.createVirtualEnv(allSdks, callback);
    }
  }

  @Override
  public boolean canBeHidden(String value) {
    return true;
  }

  @Override
  public PopupStep onChosen(final String selectedValue, boolean finalChoice) {
    return doFinalStep(new Runnable() {
      public void run() {
        optionSelected(selectedValue);
      }
    });
  }
}
