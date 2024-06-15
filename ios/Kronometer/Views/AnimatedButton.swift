//
//  AnimatedButton.swift
//  Kronometer
//
//  Created by Anze Staric on 14. 6. 24.
//

import SwiftUI

struct AnimatedButton: View {
    var action: () -> Void
    var text: String
    
    init(_ text: String, action: @escaping () -> Void) {
        self.action = action
        self.text = text
    }
    
    var body: some View {
        Button {
            withAnimation {
                action()
            }
        } label: {
            Text(text)
        }
    }
}

#Preview {
    AnimatedButton("test") { }
}
