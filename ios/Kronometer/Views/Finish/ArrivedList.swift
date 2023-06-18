//
//  ArrivedList.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import SwiftUI

struct ArrivedList: View {
    @EnvironmentObject
    private var modelData: FinishModel

    var body: some View {
        List {
            ForEach(modelData.arrived) { biker in
                FinishListItem(biker: biker, displayType: .Arrived)
            }
        }
    }
}

struct ArrivedList_Previews: PreviewProvider {
    static var previews: some View {
        ArrivedList()
            .environmentObject(FinishModel())
    }
}
