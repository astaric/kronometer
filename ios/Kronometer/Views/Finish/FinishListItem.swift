//
//  FinishListItem.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

enum DisplayType {
    case OnTrack
    case Arrived
    case Completed
}

struct FinishListItem: View {
    let biker: BikerOnFinish
    let displayType: DisplayType

    var body: some View {
        HStack {
            Text("\(biker.id)")
                .foregroundColor(.primary)
                .font(.custom("", size: 30, relativeTo: .largeTitle))
                .frame(width: 50.0)
                .padding([.trailing], 10)
            VStack {
                Text(biker.name)
                    .foregroundColor(.primary)
                    .font(.custom("", size: 20, relativeTo: .title2))
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(bikerDetails)
                    .foregroundColor(.gray)
                    .font(.custom("", size: 13, relativeTo: .title2))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            Spacer()
        }
    }

    var bikerDetails: String {
        switch displayType {
            case .OnTrack, .Arrived:
                return biker.formattedStartTime
            case .Completed:
                return biker.formattedDuration
        }
    }
}

struct FinishListItem_Previews: PreviewProvider {
    static var previews: some View {
        List {
            FinishListItem(biker: BikerOnFinish(id: 1, name: "Janez Novak", startTime: Date.now), displayType: .OnTrack)
            FinishListItem(biker: BikerOnFinish(id: 2, name: "Janez Bernik", startTime: Date.now, endTime: Date.now.addingTimeInterval(TimeInterval(integerLiteral: 100))),
                           displayType: .Completed)
            FinishListItem(biker: BikerOnFinish(id: 3, name: "Janez Krnc", startTime: Date.now),
                           displayType: .Arrived)
        }
    }
}
