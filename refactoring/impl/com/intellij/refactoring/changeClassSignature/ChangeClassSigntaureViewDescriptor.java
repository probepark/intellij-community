package com.intellij.refactoring.changeClassSignature;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.ui.UsageViewDescriptorAdapter;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author dsl
 */
public class ChangeClassSigntaureViewDescriptor extends UsageViewDescriptorAdapter {
  private PsiClass myClass;

  public ChangeClassSigntaureViewDescriptor(PsiClass aClass) {
    super();
    myClass = aClass;
  }

  public PsiElement[] getElements() {
    return new PsiElement[]{myClass};
  }

  public String getProcessedElementsHeader() {
    return StringUtil.capitalize(UsageViewUtil.getType(myClass));
  }
}
