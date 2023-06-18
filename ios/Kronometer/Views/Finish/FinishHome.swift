//
//  FinishHome.swift
//  Kronometer
//
//  Created by Anze Staric on 07/06/2023.
//

import SwiftUI

struct FinishHome: View {
    var body: some View {
        HStack {
            FinishList()
            VStack {
                ArrivedList()
                SensorEvents()
            }
            VStack{
                CompletedList()
                AddEvent()
            }
        }
    }
}

struct FinishHome_Previews: PreviewProvider {
    static var previews: some View {
        FinishHome()
            .environmentObject(FinishModel())
    }
}
