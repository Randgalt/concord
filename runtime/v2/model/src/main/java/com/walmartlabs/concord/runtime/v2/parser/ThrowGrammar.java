package com.walmartlabs.concord.runtime.v2.parser;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2020 Walmart Inc.
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

import com.walmartlabs.concord.runtime.v2.model.TaskCall;
import com.walmartlabs.concord.runtime.v2.model.TaskCallOptions;
import io.takari.parc.Parser;

import static com.walmartlabs.concord.runtime.v2.parser.GrammarMisc.satisfyField;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarOptions.simpleOptions;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarV2.anyVal;

public final class ThrowGrammar {

    public static final Parser<Atom, TaskCall> throwStep =
            satisfyField("throw", YamlValueType.TASK, a -> anyVal.bind(e ->
                    simpleOptions.map(options ->
                            new TaskCall(a.location, "throw", TaskCallOptions.builder()
                                    .putInput("exception", e)
                                    .build()))));


    private ThrowGrammar() {
    }
}
