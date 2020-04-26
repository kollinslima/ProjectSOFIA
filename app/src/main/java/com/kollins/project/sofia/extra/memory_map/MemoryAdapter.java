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

package com.kollins.project.sofia.extra.memory_map;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kollins.project.sofia.R;
import com.kollins.project.sofia.ucinterfaces.DataMemory;

import java.util.ArrayList;

import static com.kollins.project.sofia.atmega328p.DataMemory_ATmega328P.SDRAM_SIZE_TOTAL;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.ViewHolder> {

    private ArrayList<String> memAddress;
    private ArrayList<String> memAddressFiltered;
    private DataMemory dataMemory;

    public static boolean isFiltering = false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView address, b7, b6, b5, b4, b3, b2, b1, b0;

        public ViewHolder(View itemView) {
            super(itemView);
            this.address = (TextView) itemView.findViewById(R.id.address);
            this.b7 = (TextView) itemView.findViewById(R.id.bit7);
            this.b6 = (TextView) itemView.findViewById(R.id.bit6);
            this.b5 = (TextView) itemView.findViewById(R.id.bit5);
            this.b4 = (TextView) itemView.findViewById(R.id.bit4);
            this.b3 = (TextView) itemView.findViewById(R.id.bit3);
            this.b2 = (TextView) itemView.findViewById(R.id.bit2);
            this.b1 = (TextView) itemView.findViewById(R.id.bit1);
            this.b0 = (TextView) itemView.findViewById(R.id.bit0);
        }
    }

    public MemoryAdapter(DataMemory dataMemory) {
        memAddress = new ArrayList<>(SDRAM_SIZE_TOTAL);
        this.dataMemory = dataMemory;

        for (int i = 0; i < SDRAM_SIZE_TOTAL; i++) {
            memAddress.add("0x" + Integer.toHexString(i).toUpperCase());
        }

        memAddress.set(0, "R0");
        memAddress.set(1, "R1");
        memAddress.set(2, "R2");
        memAddress.set(3, "R3");
        memAddress.set(4, "R4");
        memAddress.set(5, "R5");
        memAddress.set(6, "R6");
        memAddress.set(7, "R7");
        memAddress.set(8, "R8");
        memAddress.set(9, "R9");
        memAddress.set(10, "R10");
        memAddress.set(11, "R11");
        memAddress.set(12, "R12");
        memAddress.set(13, "R13");
        memAddress.set(14, "R14");
        memAddress.set(15, "R15");
        memAddress.set(16, "R16");
        memAddress.set(17, "R17");
        memAddress.set(18, "R18");
        memAddress.set(19, "R19");
        memAddress.set(20, "R20");
        memAddress.set(21, "R21");
        memAddress.set(22, "R22");
        memAddress.set(23, "R23");
        memAddress.set(24, "R24");
        memAddress.set(25, "R25");
        memAddress.set(26, "R26");
        memAddress.set(27, "R27");
        memAddress.set(28, "R28");
        memAddress.set(29, "R29");
        memAddress.set(30, "R30");
        memAddress.set(31, "R31");

        memAddress.set(35, "PINB");
        memAddress.set(36, "DDRB");
        memAddress.set(37, "PORTB");
        memAddress.set(38, "PINC");
        memAddress.set(39, "DDRC");
        memAddress.set(40, "PORTC");
        memAddress.set(41, "PIND");
        memAddress.set(42, "DDRD");
        memAddress.set(43, "PORTD");

        memAddress.set(53, "TIFR0");
        memAddress.set(54, "TIFR1");
        memAddress.set(55, "TIFR2");

        memAddress.set(59, "PCIFR");
        memAddress.set(60, "EIFR");
        memAddress.set(61, "EIMSK");
        memAddress.set(62, "GPIOR0");
        memAddress.set(63, "EECR");
        memAddress.set(64, "EEDR");
        memAddress.set(65, "EEARL");
        memAddress.set(66, "EEARH");
        memAddress.set(67, "GTCCR");
        memAddress.set(68, "TCCR0A");
        memAddress.set(69, "TCCR0B");
        memAddress.set(70, "TCNT0");
        memAddress.set(71, "OCR0A");
        memAddress.set(72, "OCR0B");

        memAddress.set(74, "GPIOR1");
        memAddress.set(75, "GPIOR2");
        memAddress.set(76, "SPCR0");
        memAddress.set(77, "SPSR0");
        memAddress.set(78, "SPDR0");

        memAddress.set(80, "ACSR");
        memAddress.set(81, "DWDR");

        memAddress.set(83, "SMCR");
        memAddress.set(84, "MCUSR");
        memAddress.set(85, "MCUCR");

        memAddress.set(87, "SPMCSR");

        memAddress.set(93, "SPL");
        memAddress.set(94, "SPH");
        memAddress.set(95, "SREG");
        memAddress.set(96, "WDTCSR");
        memAddress.set(97, "CLKPR");

        memAddress.set(100, "PRR");

        memAddress.set(102, "OSCCAL");

        memAddress.set(104, "PCICR");
        memAddress.set(105, "EICRA");

        memAddress.set(107, "PCMSK0");
        memAddress.set(108, "PCMSK1");
        memAddress.set(109, "PCMSK2");
        memAddress.set(110, "TIMSK0");
        memAddress.set(111, "TIMSK1");
        memAddress.set(112, "TIMSK2");

        memAddress.set(120, "ADCL");
        memAddress.set(121, "ADCH");
        memAddress.set(122, "ADCSRA");
        memAddress.set(123, "ADCSRB");
        memAddress.set(124, "ADMUX");

        memAddress.set(126, "DIDR0");
        memAddress.set(127, "DIDR1");
        memAddress.set(128, "TCCR1A");
        memAddress.set(129, "TCCR1B");
        memAddress.set(130, "TCCR1C");

        memAddress.set(132, "TCNT1L");
        memAddress.set(133, "TCNT1H");
        memAddress.set(134, "ICR1L");
        memAddress.set(135, "ICR1H");
        memAddress.set(136, "OCR1AL");
        memAddress.set(137, "OCR1AH");
        memAddress.set(138, "OCR1BL");
        memAddress.set(139, "OCR1BH");

        memAddress.set(176, "TCCR2A");
        memAddress.set(177, "TCCR2B");
        memAddress.set(178, "TCNT2");
        memAddress.set(179, "OCR2A");
        memAddress.set(180, "OCR2B");

        memAddress.set(182, "ASSR");

        memAddress.set(184, "TWBR");
        memAddress.set(185, "TWSR");
        memAddress.set(186, "TWAR");
        memAddress.set(187, "TWDR");
        memAddress.set(188, "TWCR");
        memAddress.set(189, "TWAMR");

        memAddress.set(192, "UCSR0A");
        memAddress.set(193, "UCSR0B");
        memAddress.set(194, "UCSR0C");

        memAddress.set(196, "UBRR0L");
        memAddress.set(197, "UBRR0H");
        memAddress.set(198, "UDR0");

        memAddressFiltered = memAddress;
    }

    @Override
    public MemoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_memory_map, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        try {
            int realMemAdd = memAddress.indexOf(memAddressFiltered.get(position));

            holder.address.setText(memAddressFiltered.get(position));

            if (dataMemory.readBit(realMemAdd, 0)) {
                holder.b0.setText("1");
                holder.b0.setBackgroundResource(R.color.on_button);
            } else {
                holder.b0.setText("0");
                holder.b0.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 1)) {
                holder.b1.setText("1");
                holder.b1.setBackgroundResource(R.color.on_button);
            } else {
                holder.b1.setText("0");
                holder.b1.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 2)) {
                holder.b2.setText("1");
                holder.b2.setBackgroundResource(R.color.on_button);
            } else {
                holder.b2.setText("0");
                holder.b2.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 3)) {
                holder.b3.setText("1");
                holder.b3.setBackgroundResource(R.color.on_button);
            } else {
                holder.b3.setText("0");
                holder.b3.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 4)) {
                holder.b4.setText("1");
                holder.b4.setBackgroundResource(R.color.on_button);
            } else {
                holder.b4.setText("0");
                holder.b4.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 5)) {
                holder.b5.setText("1");
                holder.b5.setBackgroundResource(R.color.on_button);
            } else {
                holder.b5.setText("0");
                holder.b5.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 6)) {
                holder.b6.setText("1");
                holder.b6.setBackgroundResource(R.color.on_button);
            } else {
                holder.b6.setText("0");
                holder.b6.setBackgroundResource(R.color.off_button);
            }

            if (dataMemory.readBit(realMemAdd, 7)) {
                holder.b7.setText("1");
                holder.b7.setBackgroundResource(R.color.on_button);
            } else {
                holder.b7.setText("0");
                holder.b7.setBackgroundResource(R.color.off_button);
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Override
    public int getItemCount() {
        return memAddress.size();
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                isFiltering = true;

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    memAddressFiltered = memAddress;
                } else {
                    ArrayList<String> filteredList = new ArrayList<>();
                    for (String row : memAddress) {

                        if (row.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    memAddressFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = memAddressFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                memAddressFiltered = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
                isFiltering = false;
            }
        };
    }
}
