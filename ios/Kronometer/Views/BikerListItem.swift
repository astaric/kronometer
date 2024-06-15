//
//  BikerItem.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct BikerListItem: View {
    let biker: Biker
    var selected: Bool
    var selectable: Bool

    init(_ biker: Biker, selected: Bool = false, selectable: Bool = false) {
        self.biker = biker
        self.selected = selected
        self.selectable = selectable
    }
    
    var body: some View {
        HStack {
            if selectable {
                Rectangle()
                    .fill(selected ? Color.accentColor : .clear)
                    .frame(maxWidth: 5)
                    .offset(x: -5)
            }
            Text("\(biker.id)")
                .foregroundColor(.primary)
                .font(.largeTitle)
                .frame(minWidth: 50)
            VStack(alignment: .leading) {
                Text(biker.name)
                    .font(.title2)
                    .lineLimit(1)
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .lineLimit(1)
            }
            Spacer()
        }        
        .contentShape(Rectangle())
    }
    
    var description: String {
        if let startTime = biker.startTime,
           let endTime = biker.endTime {
            let fmt = DateComponentsFormatter()
            return fmt.string(from: startTime, to: endTime) ?? "finished"
        } else if let startTime = biker.startTime {
            return startTime.formatted(.dateTime.hour().minute().second())
        } else {
            return "waiting"
        }
    }
}

struct StartListItem_Previews: PreviewProvider {
    static var previews: some View {
        List {
            BikerListItem(Biker(id: 1, name: "Janez Novak", startTime: Date.now), selected: true)
            BikerListItem(Biker(id: 2, name: "Janez Novak", startTime: Date.now))
            BikerListItem(Biker(id: 33, name: "Janez Novak", startTime: Date.now))
        }
    }
}
