//
//  Separator.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import SwiftUI

struct Separator: View {
    var body: some View {
        Rectangle()
            .foregroundColor(.secondary)
            .frame(width: 150, height: 2)
    }
}

struct Separator_Previews: PreviewProvider {
    static var previews: some View {
        Separator()
    }
}
