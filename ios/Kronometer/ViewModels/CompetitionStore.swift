//
//  CompetitionStore.swift
//  Kronometer
//
//  Created by Anze Staric on 15. 6. 24.
//

import SwiftUI

@Observable
class CompetitionStore {
    private (set) var competitions = [Competition]()
    
    func refresh() async throws {
        competitions = try await KronometerApi.getCompetitions()
    }
}
