//
//  BikerItem.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct StartListItem: View {
    let biker: BikerOnStart

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
                Text(biker.formattedStartTime)
                    .foregroundColor(.gray)
                    .font(.custom("", size: 13, relativeTo: .title2))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            Spacer()
        }
    }
}

struct StartListItem_Previews: PreviewProvider {
    static var previews: some View {
        List {
            StartListItem(biker: BikerOnStart(id: 1, name: "Janez Novak", startTime: Date.now))
            StartListItem(biker: BikerOnStart(id: 1, name: "Janez Novak", startTime: Date.now))
            StartListItem(biker: BikerOnStart(id: 1, name: "Janez Novak", startTime: Date.now))
        }
    }
}
