//
//  BikerList.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import SwiftUI

struct BikerList<Content: View, Filters: View>: View {
    @Environment(BikerStore.self) var bikerStore

    var bikers: [Biker]
    var refreshable: Bool = false
    @State private var error: String?

    var content: (Biker) -> Content
    var filters: (() -> Filters)?

    init(
        _ bikers: [Biker], refreshable: Bool = false,
        @ViewBuilder content: @escaping (Biker) -> Content, filters: (() -> Filters)?
    ) {
        self.bikers = bikers
        self.refreshable = refreshable
        self.content = content
        self.filters = filters
    }

    var body: some View {
        VStack {
            if let filters {
                filters()
            }
            if !refreshable {
                bikerList
            } else {
                bikerList
                    .refreshable {
                        do {
                            try await bikerStore.refresh()
                        } catch {
                            self.error = error.localizedDescription
                        }
                    }
                if let error {
                    Text(error)
                }
            }
        }
    }

    var bikerList: some View {
        List {
            ForEach(bikers) { biker in
                content(biker)
            }
        }
    }
}

extension BikerList where Filters == Text {
    init(
        _ bikers: [Biker], refreshable: Bool = false,
        @ViewBuilder content: @escaping (Biker) -> Content
    ) {
        self.init(bikers, refreshable: refreshable, content: content, filters: nil)
    }
}

#Preview {
    BikerList(BikerStore().bikers) { BikerListItem($0) }
}
