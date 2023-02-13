//
//  CurrentBiker.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import SwiftUI

struct CurrentBiker: View {
    @EnvironmentObject var modelData: StartModel

    private func format(_ biker: Biker) -> String {
        if let startTime = biker.startTime {
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "HH:mm:ss"
            let startTimeText = dateFormatter.string(from: startTime)
            return "\(biker.id) \(biker.name) \(startTimeText)"
        } else {
            return "\(biker.id) \(biker.name)"
        }
    }

    var body: some View {
        VStack {
            Menu {
                Picker("Select next biker", selection: $modelData.nextBikerId) {
                    ForEach(modelData.bikers) { biker in
                        Text(format(biker))
                            .background(.red)
                            .tag(biker.id-1 as Int?)
                    }
                }
            } label: {
                Text(modelData.nextBiker?.numberAndName ?? "No more bikers")
                    .frame(maxWidth: .infinity)
                    .foregroundColor(.primary)
                    .font(.custom("", size: 30, relativeTo: .largeTitle))
                    .multilineTextAlignment(.leading)
                Image(systemName: "chevron.up.chevron.down")
                    .foregroundColor(.primary)
            }.padding()
        }.onAppear {
            if modelData.nextBiker == nil {
                modelData.selectNextBiker()
            }
        }
    }
}

struct CurrentBiker_Previews: PreviewProvider {
    static var model: StartModel {
        let model = StartModel()
        model.bikers[0].startTime = Date()
        return model
    }

    static var previews: some View {
        CurrentBiker()
            .environmentObject(model)
    }
}
