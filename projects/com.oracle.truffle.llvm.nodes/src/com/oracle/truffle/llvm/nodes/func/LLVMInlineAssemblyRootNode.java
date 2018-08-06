/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.nodes.func;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import java.util.List;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.llvm.nodes.asm.base.LLVMInlineAssemblyBlockNode;
import com.oracle.truffle.llvm.nodes.asm.base.LLVMInlineAssemblyPrologueNode;
import com.oracle.truffle.llvm.runtime.LLVMContext;
import com.oracle.truffle.llvm.runtime.LLVMLanguage;
import com.oracle.truffle.llvm.runtime.debug.scope.LLVMSourceLocation;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMExpressionNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMStatementNode;

public class LLVMInlineAssemblyRootNode extends RootNode {

    @Child private LLVMInlineAssemblyPrologueNode prologue;
    @Child private LLVMInlineAssemblyBlockNode block;
    private final LLVMSourceLocation source;

    private final LLVMExpressionNode result;

    private final ContextReference<LLVMContext> ctxRef;

    public LLVMInlineAssemblyRootNode(LLVMLanguage language, LLVMSourceLocation source, FrameDescriptor frameDescriptor,
                    LLVMStatementNode[] statements, List<LLVMStatementNode> writeNodes, LLVMExpressionNode result) {
        super(language, frameDescriptor);
        this.source = source;
        this.prologue = new LLVMInlineAssemblyPrologueNode(writeNodes);
        this.block = new LLVMInlineAssemblyBlockNode(statements);
        this.result = result;
        this.ctxRef = language.getContextReference();
    }

    @Override
    public SourceSection getSourceSection() {
        if (source != null) {
            return source.getSourceSection(ctxRef);
        }
        return null;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        prologue.execute(frame);
        block.execute(frame);
        return result == null ? 0 : result.executeGeneric(frame);
    }
}
