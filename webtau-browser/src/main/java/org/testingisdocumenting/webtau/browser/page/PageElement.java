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

package org.testingisdocumenting.webtau.browser.page;

import org.testingisdocumenting.webtau.browser.page.path.ElementsFinder;
import org.testingisdocumenting.webtau.console.ansi.Color;
import org.testingisdocumenting.webtau.data.render.PrettyPrintable;
import org.testingisdocumenting.webtau.expectation.ActualPathAndDescriptionAware;
import org.testingisdocumenting.webtau.expectation.ActualValue;
import org.testingisdocumenting.webtau.expectation.ActualValueExpectations;
import org.testingisdocumenting.webtau.expectation.ValueMatcher;
import org.testingisdocumenting.webtau.expectation.timer.ExpectationTimer;
import org.testingisdocumenting.webtau.reporter.*;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.testingisdocumenting.webtau.Matchers.*;
import static org.testingisdocumenting.webtau.console.ConsoleOutputs.out;

public interface PageElement extends
        ActualValueExpectations,
        PrettyPrintable,
        ActualPathAndDescriptionAware {

    PageElementValue<Integer> getCount();

    WebElement findElement();
    List<WebElement> findElements();

    PageElementValue<Object> elementValue();
    PageElementValue<List<Object>> elementValues();

    void setValue(Object value);
    void sendKeys(CharSequence keys);
    void click();
    void shiftClick();
    void controlClick();
    void commandClick();
    void altClick();
    void rightClick();
    void doubleClick();
    void hover();
    void clear();

    void dragAndDropOver(PageElement pageElement);

    /**
     * uses command on mac os x, and control on other OSes
     */
    default void commandOrControlClick() {
        if (BrowserConditions.isMac()) {
            commandClick();
        } else {
            controlClick();
        }
    }

    PageElement find(String css);
    PageElement find(ElementsFinder finder);
    PageElement get(String text);
    PageElement get(int number);
    PageElement get(Pattern regexp);

    boolean isVisible();
    boolean isEnabled();
    boolean isSelected();
    boolean isPresent();

    String getText();
    Object getUnderlyingValue();

    TokenizedMessage locationDescription();

    void scrollIntoView();
    void highlight();

    @Override
    default StepReportOptions shouldReportOption() {
        return StepReportOptions.REPORT_ALL;
    }

    @Override
    default void prettyPrint() {
        TokenizedMessageToAnsiConverter toAnsiConverter = IntegrationTestsMessageBuilder.getConverter();

        if (!isPresent()) {
            out(Stream.concat(
                    Stream.of(Color.RED, "element is not present: "),
                    toAnsiConverter.convert(locationDescription()).stream()).toArray());
            return;
        }

        out(Stream.concat(
                Stream.of(Color.GREEN, "element is found: "),
                toAnsiConverter.convert(locationDescription()).stream()).toArray());

        out(Color.YELLOW, "           getText(): ", Color.GREEN, getText());
        out(Color.YELLOW, "getUnderlyingValue(): ", Color.GREEN, getUnderlyingValue());
        Integer count = getCount().get();
        if (count > 1) {
            out(Color.YELLOW, "               count: ", Color.GREEN, count);
        }
    }
}
