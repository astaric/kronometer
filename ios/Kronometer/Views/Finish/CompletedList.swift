//
//  CompletedList.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import SwiftUI

struct CompletedList: View {
    @EnvironmentObject
    private var modelData: FinishModel

    var body: some View {
        List {
            ForEach(modelData.completed) { biker in
                FinishListItem(biker: biker, displayType: .Completed)
                    .swipeActions(edge: .trailing) {
                        Button {
                            modelData.undoAssignTime(biker)
                        } label: {
                            Text("Undo")
                        }
                    }
            }
        }
    }
}

struct CompletedList_Previews: PreviewProvider {
    static var previews: some View {
        CompletedList()
            .environmentObject(FinishModel())
    }
}
