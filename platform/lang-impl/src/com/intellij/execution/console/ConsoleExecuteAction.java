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
package com.intellij.execution.console;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.execution.process.ConsoleHistoryModel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConsoleExecuteAction extends DumbAwareAction {
  static final String CONSOLE_EXECUTE_ACTION_ID = "Console.Execute";

  private final LanguageConsoleView myConsoleView;
  private final LanguageConsoleImpl myConsole;
  private final ConsoleExecuteActionHandler myExecuteActionHandler;
  private final Condition<LanguageConsoleImpl> myEnabledCondition;

  @SuppressWarnings("UnusedDeclaration")
  public ConsoleExecuteAction(@NotNull LanguageConsoleView console, @NotNull BaseConsoleExecuteActionHandler executeActionHandler) {
    this(console, executeActionHandler, CONSOLE_EXECUTE_ACTION_ID, Conditions.<LanguageConsoleImpl>alwaysTrue());
  }

  ConsoleExecuteAction(@NotNull LanguageConsoleImpl console, final @NotNull ConsoleExecuteActionHandler executeActionHandler, @Nullable Condition<LanguageConsoleImpl> enabledCondition) {
    this(console, null, executeActionHandler, CONSOLE_EXECUTE_ACTION_ID, enabledCondition);
  }

  public ConsoleExecuteAction(@NotNull LanguageConsoleView console,
                              @NotNull BaseConsoleExecuteActionHandler executeActionHandler,
                              @NotNull Condition<LanguageConsoleImpl> enabledCondition) {
    this(console.getConsole(), console, executeActionHandler, CONSOLE_EXECUTE_ACTION_ID, enabledCondition);
  }

  public ConsoleExecuteAction(@NotNull LanguageConsoleView console,
                              @NotNull BaseConsoleExecuteActionHandler executeActionHandler,
                              @NotNull String emptyExecuteActionId,
                              @NotNull Condition<LanguageConsoleImpl> enabledCondition) {
    this(console.getConsole(), console, executeActionHandler, emptyExecuteActionId, enabledCondition);
  }

  private ConsoleExecuteAction(@NotNull LanguageConsoleImpl console,
                               @Nullable LanguageConsoleView consoleView,
                               @NotNull ConsoleExecuteActionHandler executeActionHandler,
                               @NotNull String emptyExecuteActionId,
                               @Nullable Condition<LanguageConsoleImpl> enabledCondition) {
    super(null, null, AllIcons.Actions.Execute);

    myConsole = console;
    myConsoleView = consoleView;
    myExecuteActionHandler = executeActionHandler;
    myEnabledCondition = enabledCondition == null ? Conditions.<LanguageConsoleImpl>alwaysTrue() : enabledCondition;

    EmptyAction.setupAction(this, emptyExecuteActionId, null);
  }

  @Override
  public final void update(AnActionEvent e) {
    EditorEx editor = myConsole.getConsoleEditor();
    Lookup lookup = LookupManager.getActiveLookup(editor);
    e.getPresentation().setEnabled(!editor.isRendererMode() && isEnabled() &&
                                   (lookup == null || !lookup.isCompletion()));
  }

  @Override
  public final void actionPerformed(AnActionEvent e) {
    myExecuteActionHandler.runExecuteAction(myConsole, myConsoleView);
  }

  protected boolean isEnabled() {
    return myEnabledCondition.value(myConsole);
  }

  static abstract class ConsoleExecuteActionHandler {
    private final ConsoleHistoryModel myConsoleHistoryModel;

    private boolean myAddToHistory = true;
    private final boolean myPreserveMarkup;

    public ConsoleExecuteActionHandler(boolean preserveMarkup) {
      myConsoleHistoryModel = new ConsoleHistoryModel();
      myPreserveMarkup = preserveMarkup;
    }

    public ConsoleHistoryModel getConsoleHistoryModel() {
      return myConsoleHistoryModel;
    }

    public void setAddCurrentToHistory(boolean addCurrentToHistory) {
      myAddToHistory = addCurrentToHistory;
    }

    final void runExecuteAction(@NotNull LanguageConsoleImpl console, @Nullable LanguageConsoleView consoleView) {
      String text = console.prepareExecuteAction(myAddToHistory, myPreserveMarkup, true);

      ((UndoManagerImpl)UndoManager.getInstance(console.getProject())).invalidateActionsFor(DocumentReferenceManager.getInstance().create(console.getCurrentEditor().getDocument()));

      myConsoleHistoryModel.addToHistory(text);
      doExecute(text, console, consoleView);
    }

    abstract void doExecute(@NotNull String text, @NotNull LanguageConsoleImpl console, @Nullable LanguageConsoleView consoleView);
  }
}