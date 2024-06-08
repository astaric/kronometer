//
//  BikerItem.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct StartListItem: View {
    let biker: Biker
    var selected: Bool = false

    var body: some View {
        HStack {
            Rectangle()
                .fill(selected ? Color.accentColor : .clear)
                .frame(maxWidth: 5)
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
                Text(startTime)
                    .foregroundColor(.gray)
                    .font(.custom("", size: 13, relativeTo: .title2))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            Spacer()
        }        
        .contentShape(Rectangle())
    }
    
    var startTime: String {
        biker.startTime?.formatted(.dateTime.hour().minute().second()) ?? "waiting"
    }
}

struct StartListItem_Previews: PreviewProvider {
    static var previews: some View {
        List {
            StartListItem(biker: Biker(id: 1, name: "Janez Novak", startTime: Date.now), selected: true)
            StartListItem(biker: Biker(id: 1, name: "Janez Novak", startTime: Date.now))
            StartListItem(biker: Biker(id: 1, name: "Janez Novak", startTime: Date.now))
        }
    }
}
