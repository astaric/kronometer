//
//  ModelData.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import Foundation

final class StartModel: ObservableObject {    
    var bikers = dummyBikers()
    @Published var nextBikerId: Int?
    var nextBiker: Biker? {
        if let nextBikerId = nextBikerId {
            return bikers[nextBikerId]
        } else {
            return nil
        }
    }
}



extension StartModel {
    func selectNextBiker() {
        var startIdx = 0
        if let nextBiker = nextBiker {
            startIdx = bikers.firstIndex(of: nextBiker)! + 1
            if startIdx >= bikers.count {
                startIdx = 0
            }
        }
        if startIdx < bikers.count {
            for i in startIdx..<bikers.count {
                if bikers[i].startTime == nil {
                    nextBikerId = i
                    return
                }
            }
        }
        nextBikerId = nil
    }

    func startBiker() {
        if let nextBikerId = nextBikerId {
            bikers[nextBikerId].startTime = Date()
        }
        selectNextBiker()
    }
}


func dummyBikers() -> [Biker] {
    var names = [String]()
    for surname in ["Meglič", "Samsa", "Tolminec", "Novak", "Groš"] {
        for name in ["Janez", "Peter", "Matej", "Igor", "Niko"] {
            names.append("\(name) \(surname)")
        }
    }
    names.shuffle()
    return names.enumerated().map { (i, name) in Biker(id: i+1, name: name)}
}
