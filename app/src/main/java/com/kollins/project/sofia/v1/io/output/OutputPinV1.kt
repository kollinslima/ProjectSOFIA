package com.kollins.project.sofia.v1.io.output

enum class OutputState {
    HIGH,
    LOW,
    HI_Z
}

class OutputPinV1() {
    private var state: OutputState = OutputState.HI_Z
}