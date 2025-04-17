//
//  BluetoothStatus.swift
//  Kronometer
//
//  Created by Anze Staric on 8. 6. 24.
//

import SwiftUI

struct BluetoothStatus: View {
    @EnvironmentObject var bleController: SensorController

    var body: some View {
        BTIcon()
            .stroke()
            .aspectRatio(1 / 2, contentMode: .fit)
            .foregroundStyle(bleController.sensorConnected ? Color.primary : Color.red)
            .frame(minHeight: 20)
    }
}

struct BTIcon: InsettableShape {
    typealias InsetShape = BTIcon
    var inset = CGFloat.zero

    func path(in rect: CGRect) -> Path {
        let rect = insetRect(rect)

        let yu = rect.height / 4
        return Path { path in
            path.move(to: CGPoint(x: rect.minX, y: yu))
            path.addLine(to: CGPoint(x: rect.maxX, y: 3 * yu))
            path.addLine(to: CGPoint(x: rect.midX, y: rect.maxY))
            path.addLine(to: CGPoint(x: rect.midX, y: rect.minY))
            path.addLine(to: CGPoint(x: rect.maxX, y: yu))
            path.addLine(to: CGPoint(x: rect.minY, y: 3 * yu))
        }
    }

    func inset(by amount: CGFloat) -> BTIcon {
        var shape = self
        shape.inset += amount
        return shape
    }

    func insetRect(_ rect: CGRect) -> CGRect {
        let a = inset * sqrt(2)
        return CGRect(
            x: rect.minX + a, y: rect.minY + a, width: rect.width - 2 * a,
            height: rect.height - 2 * a)
    }
}

#Preview {
    BluetoothStatus()
        .foregroundStyle(.red)
        .frame(maxHeight: 20)
}
