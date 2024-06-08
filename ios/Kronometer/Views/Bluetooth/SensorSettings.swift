//
//  SensorSettings.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct SensorSettings: View {
    var body: some View {
        VStack {
            Sensors()
            SensorLog()
        }
    }
}

struct SensorSettings_Previews: PreviewProvider {
    static var previews: some View {
        SensorSettings()
    }
}
