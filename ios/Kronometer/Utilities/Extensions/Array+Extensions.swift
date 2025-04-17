//
//  Array+Extensions.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//

extension Array {
    func selectNext(after idx: Self.Index?, predicate: (Self.Element) -> Bool) -> Element? {
        let idx = idx ?? self.endIndex
        var nextIdx = self.index(after: idx)
        if nextIdx >= self.endIndex {
            nextIdx = self.startIndex
        }
        while nextIdx != idx {
            if predicate(self[nextIdx]) {
                return self[nextIdx]
            }
            nextIdx = self.index(after: nextIdx)
            if nextIdx == idx {
                break
            }
            if nextIdx >= self.endIndex {
                nextIdx = self.startIndex
            }
        }
        return nil
    }
}
