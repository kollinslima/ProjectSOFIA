/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kollins.project.sofia;

import android.content.res.Resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UCModule_View.class, UCModule.class, Resources.class})
public class UCModule_ViewTest {

    private Field simulatedTimeField;
    private UCModule_View ucModule_view;
    private UCModule ucModule;

    @BeforeEach
    public void prepareForTest() throws Exception {

        ucModule_view = new UCModule_View();
        ucModule = new UCModule();
        simulatedTimeField = UCModule_View.class.getDeclaredField("simulatedTime");
        simulatedTimeField.setAccessible(true);
    }

    @org.junit.jupiter.api.Test
    public void countTime_simulatedOnly() throws IllegalAccessException {
        simulatedTimeField.set(null, 0);
        ucModule_view.run();
        assertEquals(UCModule_View.CLOCK_PERIOD, simulatedTimeField.get(null));
    }

    @Test
    public void countTime_microseconds() throws Exception {
        simulatedTimeField.set(null, 10000);
        Whitebox.setInternalState(ucModule_view,"delayScreenUpdateCount", UCModule_View.DELAY_SCREEN_UPDATE);

        try {
            ucModule_view.run();
        } catch (NullPointerException e){}

        assertEquals((long) 1, Whitebox.getInternalState(ucModule_view,"microSeconds"));
    }

    @Test
    public void reset() throws Exception {
        simulatedTimeField.set(null, 10000);

        ucModule_view.resetIO();

        assertEquals((long) 0, simulatedTimeField.get(null));
    }
}