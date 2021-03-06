/*
 * Copyright 2020 webtau maintainers
 * Copyright 2019 TWO SIGMA OPEN SOURCE, LLC
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

import org.testingisdocumenting.webtau.console.ConsoleOutputs;
import org.testingisdocumenting.webtau.reporter.WebTauStep;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.testingisdocumenting.webtau.cfg.WebTauConfig.getCfg;

class StreamGobbler implements Runnable {
    private final InputStream stream;
    private final boolean isSilent;

    private final List<String> lines;
    private final boolean renderOutput;

    private IOException exception;

    public StreamGobbler(InputStream stream, boolean isSilent) {
        this.stream = stream;
        this.isSilent = isSilent;
        this.lines = new ArrayList<>();
        this.renderOutput = shouldRenderOutput();
    }

    public List<String> getLines() {
        return lines;
    }

    public int getNumberOfLines() {
        return lines.size();
    }

    synchronized public List<String> copyLinesStartingAt(int idx) {
        return new ArrayList<>(lines.subList(idx, lines.size()));
    }

    public String joinLines() {
        return String.join("\n", lines);
    }

    public String joinLinesStartingAt(int idx) {
        return String.join("\n", copyLinesStartingAt(idx));
    }

    public IOException getException() {
        return exception;
    }

    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        try {
            consume(bufferedReader);
        } catch (IOException e) {
            exception = e;
        }
    }

    private void consume(BufferedReader bufferedReader) throws IOException {
        for (;;) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }

            if (renderOutput) {
                ConsoleOutputs.out(line);
            }

            addLine(line);
        }
    }

    synchronized private void addLine(String line) {
        lines.add(line);
    }

    private boolean shouldRenderOutput() {
        if (isSilent) {
            return false;
        }

        WebTauStep currentStep = WebTauStep.getCurrentStep();
        int numberOfParents = currentStep == null ? 0 : currentStep.getNumberOfParents();

        return getCfg().getVerbosityLevel() > numberOfParents + 1;
    }
}
