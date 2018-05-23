package com.example.kollins.sofia.ATmega328P;

import android.util.Log;

import com.example.kollins.sofia.uCInterfaces.DataMemory;
import com.example.kollins.sofia.uCInterfaces.InterruptionModule;

public class InterruptionModule_ATmega328P implements InterruptionModule {

    private static final String INTERRUPTION_TAG = "Interruption";

    private static final int POINTER_ADDR_INT0 = 0;
    private static final int POINTER_ADDR_INT1 = 1;
    private static final int POINTER_ADDR_PCINT0 = 2;
    private static final int POINTER_ADDR_PCINT1 = 3;
    private static final int POINTER_ADDR_PCINT2 = 4;
    private static final int POINTER_ADDR_TIMER2_COMP_A = 6;
    private static final int POINTER_ADDR_TIMER2_COMP_B = 7;
    private static final int POINTER_ADDR_TIMER2_OVERFLOW = 8;
    private static final int POINTER_ADDR_TIMER1_CAPTURE_EVENT = 9;
    private static final int POINTER_ADDR_TIMER1_COMP_A = 10;
    private static final int POINTER_ADDR_TIMER1_COMP_B = 11;
    private static final int POINTER_ADDR_TIMER1_OVERFLOW = 12;
    private static final int POINTER_ADDR_TIMER0_COMP_A = 13;
    private static final int POINTER_ADDR_TIMER0_COMP_B = 14;
    private static final int POINTER_ADDR_TIMER0_OVERFLOW = 15;
    private static final int POINTER_ADDR_ADC = 20;

    private static final char[] INTERRUPT_VECTOR = {
            0x0002,  //INT0
            0x0004,  //INT1
            0x0006,  //PCINT0
            0x0008,  //PCINT1
            0x000A,  //PCINT2
            0x000C,  //WDT
            0x000E,  //TIMER2_COMPA
            0x0010,  //TIMER2_COMPB
            0x0012,  //TIMER2_OVF
            0x0014,  //TIMER1_CAPT
            0x0016,  //TIMER1_COMPA
            0x0018,  //TIMER1_COMPB
            0x001A,  //TIMER1_OVF
            0x001C,  //TIMER0_COMPA
            0x001E,  //TIMER0_COMPB
            0x0020,  //TIMER0_OVF
            0x0022,  //SPI_STC
            0x0024,  //USART_RX
            0x0026,  //USART_UDRE
            0x0028,  //USART_TX
            0x002A,  //ADC
            0x002C,  //EE_READY
            0x002E,  //ANALOG_COMP
            0x0030,  //TWI
            0x0030,  //SPM_READY
    };

    private DataMemory_ATmega328P dataMemory;
    private char pcInterruption;

    @Override
    public boolean haveInterruption() {
        Log.i(INTERRUPTION_TAG, "CPU Interruption check");
        //Is global interruption enabled?
        if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 7)) {

            //Check INT0
            if (dataMemory.readBit(DataMemory_ATmega328P.EIMSK_ADDR, 0)) {
                byte configINT0 = dataMemory.readByte(DataMemory_ATmega328P.EICRA_ADDR);
                if ((0x03 & configINT0) == 0x00) {
                    //Level interruption
                    if (!dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 2)) {
                        Log.i(INTERRUPTION_TAG, "INT0 Level Found");
                        pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_INT0];
                        return true;
                    }
                } else {
                    if (dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0)) {

                        //Interruption will execute -> Clear flag
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);
                        Log.i(INTERRUPTION_TAG, "INT0 Found");
                        pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_INT0];
                        return true;
                    }
                }
            }

            //Check INT1
            if (dataMemory.readBit(DataMemory_ATmega328P.EIMSK_ADDR, 1)) {
                byte configINT1 = dataMemory.readByte(DataMemory_ATmega328P.EICRA_ADDR);
                if ((0x0C & configINT1) == 0x00) {
                    //Level interruption
                    if (!dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 3)) {
                        Log.i(INTERRUPTION_TAG, "INT1 Level Found");
                        pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_INT1];
                        return true;
                    }
                } else {
                    if (dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1)) {

                        //Interruption will execute -> Clear flag
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);
                        Log.i(INTERRUPTION_TAG, "INT1 Found");
                        pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_INT1];
                        return true;
                    }
                }
            }

            //Check PCINT0
            if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 0)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 0)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, false);
                    Log.i(INTERRUPTION_TAG, "PCINT0 Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_PCINT0];
                    return true;
                }
            }

            //Check PCINT1
            if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 1)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 1)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, false);
                    Log.i(INTERRUPTION_TAG, "PCINT1 Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_PCINT1];
                    return true;
                }
            }

            //Check PCINT2
            if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 2)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 2)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, false);
                    Log.i(INTERRUPTION_TAG, "PCINT2 Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_PCINT2];
                    return true;
                }
            }

            //Check TIMER2 CompA
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK2_ADDR, 1)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 1)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 1, false);
                    Log.i(INTERRUPTION_TAG, "TIMER2 COMP_A Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_COMP_A];
                    return true;
                }
            }

            //Check TIMER2 CompB
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK2_ADDR, 2)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 2)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 2, false);
                    Log.i(INTERRUPTION_TAG, "TIMER2 COMP_B Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_COMP_B];
                    return true;
                }
            }

            //Check TIMER2 Overflow
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK2_ADDR, 0)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 0)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 0, false);
                    Log.i(INTERRUPTION_TAG, "TIMER2 OVERLOW Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_OVERFLOW];
                    return true;
                }
            }

            //Check TIMER1 Capture Event
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK1_ADDR, 5)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 5)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, false);
                    Log.i(INTERRUPTION_TAG, "TIMER1 CAPTURE EVENT Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_CAPTURE_EVENT];
                    return true;
                }
            }

            //Check TIMER1 CompA
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK1_ADDR, 1)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 1)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 1, false);
                    Log.i(INTERRUPTION_TAG, "TIMER1 COMP_A Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_COMP_A];
                    return true;
                }
            }

            //Check TIMER1 CompB
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK1_ADDR, 2)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 2)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, false);
                    Log.i(INTERRUPTION_TAG, "TIMER1 COMP_B Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_COMP_B];
                    return true;
                }
            }

            //Check TIMER1 Overflow
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK1_ADDR, 0)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 0)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, false);
                    Log.i(INTERRUPTION_TAG, "TIMER1 OVERFLOW Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_OVERFLOW];
                    return true;
                }
            }


            //Check TIMER0 CompA
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK0_ADDR, 1)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 1)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, false);
                    Log.i(INTERRUPTION_TAG, "TIMER0 COMP_A Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_COMP_A];
                    return true;
                }
            }

            //Check TIMER0 CompB
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK0_ADDR, 2)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 2)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 2, false);
                    Log.i(INTERRUPTION_TAG, "TIMER0 COMP_B Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_COMP_B];
                    return true;
                }
            }

            //Check TIMER0 Overflow
            if (dataMemory.readBit(DataMemory_ATmega328P.TIMSK0_ADDR, 0)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 0)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, false);
                    Log.i(INTERRUPTION_TAG, "TIMER0 OVERFLOW Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_OVERFLOW];
                    return true;
                }
            }

            //Check ADC
            if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 3)) {
                if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4)) {

                    //Interruption will execute -> Clear flag
                    dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, false);
                    Log.i(INTERRUPTION_TAG, "ADC Found");
                    pcInterruption = INTERRUPT_VECTOR[POINTER_ADDR_ADC];
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public char getPCInterruptionAddress() {
        return pcInterruption;
    }

    @Override
    public void disableGlobalInterruptions() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);
    }

    @Override
    public void enableGlobalInterruptions() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);
    }

    @Override
    public void timer0Overflow() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, true);

        //Auto Trigger ADC
        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 4) {
            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
        }
    }

    @Override
    public void timer0MatchA() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, true);

        //Auto Trigger ADC
        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 3) {
            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
        }
    }

    @Override
    public void timer0MatchB() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 2, true);
    }

    @Override
    public void timer1Overflow() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, true);

        //Auto Trigger ADC
        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 6) {
            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
        }
    }

    @Override
    public void timer1MatchA() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 1, true);
    }

    @Override
    public void timer1MatchB() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, true);

        //Auto Trigger ADC
        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 5) {
            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
        }
    }

    @Override
    public void timer1InputCapture() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, true);

        //Auto Trigger ADC
        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 7) {
            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
        }
    }

    @Override
    public void timer2Overflow() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 0, true);
    }

    @Override
    public void timer2MatchA() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 1, true);
    }

    @Override
    public void timer2MatchB() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 2, true);
    }

    @Override
    public void conversionCompleteADC() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, true);
    }

    @Override
    public void checkIOInterruption(int pinAddress, int pinPosition, boolean oldState, boolean newState) {
        Log.v(INTERRUPTION_TAG, "Check IO Interruption");
        Log.v(INTERRUPTION_TAG, "Address: " + Integer.toHexString(pinAddress));
        Log.v(INTERRUPTION_TAG, "Position: " + pinPosition);
        Log.v(INTERRUPTION_TAG, "Old state: " + oldState);
        Log.v(INTERRUPTION_TAG, "New state: " + newState);

        if (pinAddress == DataMemory_ATmega328P.PIND_ADDR) {
            if (pinPosition == 2) {
                if (!checkINT0Interruption(oldState, newState)) {
                    Log.v(INTERRUPTION_TAG, "INT0 not detected");
                    checkPCINT2Interruption(pinPosition, oldState, newState);
                }
            } else if (pinPosition == 3) {
                if (!checkINT1Interruption(oldState, newState)) {
                    Log.v(INTERRUPTION_TAG, "INT1 not detected");
                    checkPCINT2Interruption(pinPosition, oldState, newState);
                }
            } else {
                checkPCINT2Interruption(pinPosition, oldState, newState);
            }
        } else if (pinAddress == DataMemory_ATmega328P.PINB_ADDR) {
            checkPCINT0Interruption(pinPosition, oldState, newState);
        } else {    //PINC
            checkPCINT1Interruption(pinPosition, oldState, newState);
        }
    }

    private boolean checkINT0Interruption(boolean oldState, boolean newState) {

        //Is INT0 enabled?
        if (dataMemory.readBit(DataMemory_ATmega328P.EIMSK_ADDR, 0)) {
            byte configINT0 = dataMemory.readByte(DataMemory_ATmega328P.EICRA_ADDR);

            switch (0x03 & configINT0) {
                case 0x00:
                    //Level interrupt
                    if (!newState) {
                        Log.v(INTERRUPTION_TAG, "INT0 Level");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

                        //Auto Trigger ADC
                        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 2) {
                            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
                        }

                        return true;
                    }
                    break;
                case 0x01:
                    //Change interrupt
                    if (oldState ^ newState) {
                        Log.v(INTERRUPTION_TAG, "INT0 Change");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

                        //Auto Trigger ADC
                        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 2) {
                            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
                        }
                        return true;
                    }
                    break;
                case 0x02:
                    //Falling edge interrupt
                    if (oldState && !newState) {
                        Log.v(INTERRUPTION_TAG, "INT0 Falling Edge");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

                        //Auto Trigger ADC
                        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 2) {
                            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
                        }
                        return true;
                    }
                    break;
                case 0x03:
                    //Rising edge interrupt
                    if (!oldState && newState) {
                        Log.v(INTERRUPTION_TAG, "INT0 Rising Edge");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

                        //Auto Trigger ADC
                        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5)
                                && (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) == 2) {
                            dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);
                        }
                        return true;
                    }
                    break;

                default:
                    break;
            }
        }
        return false;
    }

    private boolean checkINT1Interruption(boolean oldState, boolean newState) {

        //Is INT1 enabled?
        if (dataMemory.readBit(DataMemory_ATmega328P.EIMSK_ADDR, 1)) {
            byte configINT0 = dataMemory.readByte(DataMemory_ATmega328P.EICRA_ADDR);

            switch (0x0C & configINT0) {
                case 0x00:
                    //Level interrupt
                    if (!newState) {
                        Log.v(INTERRUPTION_TAG, "INT1 Level");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);
                        return true;
                    }
                    break;
                case 0x04:
                    //Change interrupt
                    if (oldState ^ newState) {
                        Log.v(INTERRUPTION_TAG, "INT1 Change");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);
                        return true;
                    }
                    break;
                case 0x08:
                    //Falling edge interrupt
                    if (oldState && !newState) {
                        Log.v(INTERRUPTION_TAG, "INT1 Falling Edge");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);
                        return true;
                    }
                    break;
                case 0x0C:
                    //Rising edge interrupt
                    if (!oldState && newState) {
                        Log.v(INTERRUPTION_TAG, "INT1 Rising Edge");
                        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void checkPCINT2Interruption(int pinPosition, boolean oldState, boolean newState) {
        if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 2)) {
            //PCINT2 enabled
            if (dataMemory.readBit(DataMemory_ATmega328P.PCMSK2_ADDR, pinPosition)) {
                //PIN interruption enabled
                if (oldState ^ newState) {
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, true);
                }
            }
        }
    }

    private void checkPCINT1Interruption(int pinPosition, boolean oldState, boolean newState) {
        if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 1)) {
            //PCINT2 enabled
            if (dataMemory.readBit(DataMemory_ATmega328P.PCMSK1_ADDR, pinPosition)) {
                //PIN interruption enabled
                if (oldState ^ newState) {
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, true);
                    Log.v(INTERRUPTION_TAG, "Write: " + dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 1));
                }
            }
        }
    }

    private void checkPCINT0Interruption(int pinPosition, boolean oldState, boolean newState) {
        if (dataMemory.readBit(DataMemory_ATmega328P.PCICR_ADDR, 0)) {
            //PCINT2 enabled
            if (dataMemory.readBit(DataMemory_ATmega328P.PCMSK0_ADDR, pinPosition)) {
                //PIN interruption enabled
                if (oldState ^ newState) {
                    dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, true);
                }
            }
        }
    }

    @Override
    public void setMemory(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
    }
}

