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
package com.intellij.psi;

import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class PsiCapturedWildcardType extends PsiType {
  @NotNull private final PsiWildcardType myExistential;
  @NotNull private final PsiElement myContext;
  @Nullable private final PsiTypeParameter myParameter;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsiCapturedWildcardType)) return false;
    final PsiCapturedWildcardType captured = (PsiCapturedWildcardType)o;
    if (!myContext.equals(captured.myContext) || 
        !myExistential.equals(captured.myExistential)) return false;

    if (myContext instanceof PsiReferenceExpression) {
      if (myParameter != null ? !myParameter.equals(captured.myParameter) : captured.myParameter != null) return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return myExistential.hashCode() + 31 * myContext.hashCode();
  }

  private PsiCapturedWildcardType(@NotNull PsiWildcardType existential, @NotNull PsiElement context, @Nullable PsiTypeParameter parameter) {
    super(PsiAnnotation.EMPTY_ARRAY);//todo
    myExistential = existential;
    myContext = context;
    myParameter = parameter;
  }

  @NotNull
  public static PsiCapturedWildcardType create(@NotNull PsiWildcardType existential, @NotNull PsiElement context) {
    return create(existential, context, null);
  }

  @NotNull
  public static PsiCapturedWildcardType create(@NotNull PsiWildcardType existential,
                                               @NotNull PsiElement context,
                                               PsiTypeParameter parameter) {
    return new PsiCapturedWildcardType(existential, context, parameter);
  }

  @NotNull
  @Override
  public String getPresentableText() {
    return myExistential.getPresentableText();
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myExistential.getCanonicalText();
  }

  @NotNull
  @Override
  public String getInternalCanonicalText() {
    //noinspection HardCodedStringLiteral
    return "capture<" + myExistential.getInternalCanonicalText() + '>';
  }

  @Override
  public boolean isValid() {
    return myExistential.isValid();
  }

  @Override
  public boolean equalsToText(@NotNull String text) {
    return false;
  }

  @Override
  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return visitor.visitCapturedWildcardType(this);
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return myExistential.getResolveScope();
  }

  @Override
  @NotNull
  public PsiType[] getSuperTypes() {
    return myExistential.getSuperTypes();
  }

  public PsiType getLowerBound () {
    return myExistential.isSuper() ? myExistential.getBound() : NULL;
  }

  public PsiType getUpperBound () {
    final PsiType bound = myExistential.getBound();
    if (myExistential.isExtends()) {
      return bound;
    }
    else {
      return bound instanceof PsiCapturedWildcardType
             ? PsiWildcardType.createSuper(myContext.getManager(), ((PsiCapturedWildcardType)bound).getUpperBound())
             : PsiType.getJavaLangObject(myContext.getManager(), getResolveScope());
    }
  }

  @NotNull
  public PsiWildcardType getWildcard() {
    return myExistential;
  }

  @NotNull
  public PsiElement getContext() {
    return myContext;
  }
}
