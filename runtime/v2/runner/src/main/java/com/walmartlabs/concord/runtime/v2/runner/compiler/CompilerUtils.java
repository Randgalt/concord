package com.walmartlabs.concord.runtime.v2.runner.compiler;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2019 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.runtime.v2.model.ProcessDefinition;
import com.walmartlabs.concord.runtime.v2.model.Step;
import com.walmartlabs.concord.runtime.v2.sdk.Compiler;
import com.walmartlabs.concord.svm.Command;
import com.walmartlabs.concord.svm.commands.Block;

import java.util.List;
import java.util.stream.Collectors;

public final class CompilerUtils {

    public static Command compile(Compiler compiler, ProcessDefinition pd, String flowName) {
        List<Step> steps = pd.flows().get(flowName);
        if (steps == null) {
            throw new IllegalArgumentException("Flow not found: " + flowName);
        }

        List<Command> commands = steps.stream().map(s -> compiler.compile(pd, s)).collect(Collectors.toList());
        return new Block(commands);
    }

    public static Command compile(CompilerContext context, List<Step> steps) {
        return new Block(steps.stream()
                .map(s -> context.compiler().compile(context.processDefinition(), s))
                .collect(Collectors.toList()));
    }

    private CompilerUtils() {
    }
}
