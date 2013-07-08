/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.JetNodeType;

abstract class JetExpressionImpl extends JetElementImpl implements JetExpression {
    public JetExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitExpression(this);
    }

    @Override
    public <R, D> R accept(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitExpression(this, data);
    }

    protected JetExpression findExpressionUnder(JetNodeType type) {
        JetContainerNode containerNode = (JetContainerNode) findChildByType(type);
        if (containerNode == null) return null;

        return containerNode.findChildByClass(JetExpression.class);
    }

    @Override
    public void replaceChildInternal(PsiElement child, TreeElement newElement) {
        PsiElement newPsi = newElement.getPsi();
        if (newPsi instanceof JetExpression && JetPsiUtil.areParenthesesNecessary((JetExpression) newPsi, ((JetExpression) child), this)) {
            newElement = (TreeElement) JetPsiFactory.createExpression(getProject(), "(" + newElement.getText() + ")").getNode();
        }
        super.replaceChildInternal(child, newElement);
    }
}
