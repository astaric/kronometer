//
//  Settings.swift
//  Kronometer
//
//  Created by Anze Staric on 14. 6. 24.
//

import SwiftUI

struct Settings: View {
    @Environment(CountdownCounter.self) var countdown
    
    @State var showDebug = false
    
    var body: some View {
        Form {
            Section("Štart") {
                @Bindable var binding = countdown
                Stepper("Čas med tekmovalci: \(self.countdown.defaultCountdown) sekund", value: $binding.defaultCountdown)
            }
            
            Section("Napredno") {
                Toggle(isOn: $showDebug, label: {
                    Text("Napredne nastavitve")
                })
                if showDebug {
                    DebugActions()
                }
            }
        }
    }
}

#Preview {
    return Settings()
        .environment(CountdownCounter())
    
}
