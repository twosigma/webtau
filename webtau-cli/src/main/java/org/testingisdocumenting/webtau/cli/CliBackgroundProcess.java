/*
 * Copyright 2020 webtau maintainers
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

package org.testingisdocumenting.webtau.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class CliBackgroundProcess {
    private final Process process;
    private final String command;
    private final StreamGobbler outputGobbler;
    private final StreamGobbler errorGobbler;

    private final int pid;

    private final Thread consumeErrorThread;
    private final Thread consumeOutThread;

    private final CliOutput output;
    private final CliOutput error;

    private final AtomicBoolean isActive;

    public CliBackgroundProcess(Process process,
                                String command,
                                StreamGobbler outputGobbler,
                                StreamGobbler errorGobbler,
                                Thread consumeErrorThread,
                                Thread consumeOutThread) {
        this.process = process;
        this.pid = extractPid(process);
        this.command = command;
        this.outputGobbler = outputGobbler;
        this.errorGobbler = errorGobbler;
        this.consumeErrorThread = consumeErrorThread;
        this.consumeOutThread = consumeOutThread;
        this.output = new CliOutput(command, "process output", outputGobbler);
        this.error = new CliOutput(command, "process error output", errorGobbler);
        this.isActive = new AtomicBoolean(true);
    }

    public Process getProcess() {
        return process;
    }

    public int getPid() {
        return pid;
    }

    public int exitCode() {
        return process.exitValue();
    }

    public String getCommand() {
        return command;
    }

    public void destroy() {
        try {
            ProcessUtils.kill(pid);
            process.waitFor();
            outputGobbler.close();
            errorGobbler.close();
            isActive.set(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAsInactive() {
        isActive.set(false);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void send(String line) {
        OutputStream outputStream = process.getOutputStream();
        try {
            outputStream.write(line.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearOutput() {
        output.clear();
        error.clear();
    }

    public CliOutput getOutput() {
        return output;
    }

    public CliOutput getError() {
        return error;
    }

    public Thread getConsumeErrorThread() {
        return consumeErrorThread;
    }

    public Thread getConsumeOutThread() {
        return consumeOutThread;
    }

    public ProcessRunResult createRunResult() {
        return new ProcessRunResult(process.exitValue(), output, error);
    }

    List<String> getOutputStartingAtIdx(int idx) {
        return output.copyLinesStartingAtIdx(idx);
    }

    List<String> getErrorStartingAtIdx(int idx) {
        return error.copyLinesStartingAtIdx(idx);
    }

    /**
     * we need to support java 8, pid() is added in java 9
     * so using hacks to get to pid value
     * @param process process
     * @return pid
     */
    private static int extractPid(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            return (int) pidField.get(process);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
