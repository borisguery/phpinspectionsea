package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

public class AssertNotSameStrategy {
    final static String message = "This check is type-unsafe, consider using assertNotSame instead";

    static public void apply(@NotNull String function, @NotNull MethodReference reference, @NotNull ProblemsHolder holder) {
        final PsiElement[] params = reference.getParameters();
        if (2 == params.length && function.equals("assertNotEquals")) {
            final TheLocalFix fixer = new TheLocalFix(params[0], params[1]);
            holder.registerProblem(reference, message, ProblemHighlightType.WEAK_WARNING, fixer);
        }
    }

    private static class TheLocalFix implements LocalQuickFix {
        private PsiElement first;
        private PsiElement second;

        TheLocalFix(@NotNull PsiElement first, @NotNull PsiElement second) {
            super();
            this.first  = first;
            this.second = second;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use ::assertNotSame";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                final FunctionReference replacement = PhpPsiElementFactory.createFunctionReference(project, "pattern(null, null)");
                replacement.getParameters()[0].replace(this.first);
                replacement.getParameters()[1].replace(this.second);

                final FunctionReference call = (FunctionReference) expression;
                //noinspection ConstantConditions I'm really sure NPE will not happen
                call.getParameterList().replace(replacement.getParameterList());
                call.handleElementRename("assertNotSame");
            }
        }
    }
}