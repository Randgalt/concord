/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
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

import * as React from 'react';
import { useCallback, useState } from 'react';
import { Route } from 'react-router';
import { Divider } from 'semantic-ui-react';

import { get as apiGet, isFinal, ProcessEntry } from '../../../api/process';
import { FormListEntry, list as apiListForms } from '../../../api/process/form';

import { usePolling } from '../../../api/usePolling';
import { ProcessActionList, ProcessStatusTable } from '../../molecules';
import ProcessCheckpointActivity from '../ProcessCheckpointActivity';
import RequestErrorActivity from '../RequestErrorActivity';

import './styles.css';
import { ConcordId } from '../../../api/common';

interface ExternalProps {
    instanceId: ConcordId;
    forceRefresh: boolean;
    loadingHandler: (inc: number) => void;
    refreshHandler: () => void;
}

const DATA_FETCH_INTERVAL = 5000;

const ProcessStatusActivity = ({
    instanceId,
    loadingHandler,
    forceRefresh,
    refreshHandler
}: ExternalProps) => {
    const [process, setProcess] = useState<ProcessEntry>();
    const [forms, setForms] = useState<FormListEntry[]>([]);

    const fetchData = useCallback(async () => {
        const process = await apiGet(instanceId, ['checkpoints', 'history']);
        setProcess(process);

        const forms = await apiListForms(instanceId);
        setForms(forms);

        return !isFinal(process.status);
    }, [instanceId]);

    const error = usePolling(fetchData, DATA_FETCH_INTERVAL, loadingHandler, forceRefresh);

    if (error) {
        return <RequestErrorActivity error={error} />;
    }

    const hasCheckpoints = process && process.checkpoints && process.checkpoints.length > 0;
    const hasStatusHistory = process && process.statusHistory && process.statusHistory.length > 0;

    return (
        <>
            <ProcessStatusTable process={process} />

            {process && forms.length > 0 && !isFinal(process.status) && (
                <>
                    <Divider content="Required Actions" horizontal={true} />
                    <Route
                        render={({ history }) => (
                            <ProcessActionList
                                instanceId={instanceId}
                                forms={forms}
                                onOpenWizard={() =>
                                    history.push(`/process/${instanceId}/wizard?fullScreen=true`)
                                }
                            />
                        )}
                    />
                </>
            )}

            {process && hasCheckpoints && hasStatusHistory && (
                <>
                    <Divider content="Checkpoints" horizontal={true} />
                    <ProcessCheckpointActivity
                        instanceId={process.instanceId}
                        processStatus={process.status}
                        processDisabled={process.disabled}
                        checkpoints={process.checkpoints!}
                        statusHistory={process.statusHistory!}
                        onRestoreComplete={refreshHandler}
                    />
                </>
            )}
        </>
    );
};

export default ProcessStatusActivity;
