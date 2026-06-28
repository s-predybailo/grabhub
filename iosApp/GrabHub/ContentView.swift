import SwiftUI
import Shared

struct ContentView: View {
    @State private var query = ""
    @State private var results: [ModelItemWrapper] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    private let searchFacade = SearchFacade()

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                HStack {
                    TextField("Search 3D models…", text: $query)
                        .textFieldStyle(.roundedBorder)
                    Button("Search") { performSearch() }
                        .disabled(query.trimmingCharacters(in: .whitespaces).isEmpty || isLoading)
                }
                .padding(.horizontal)

                if isLoading {
                    ProgressView()
                        .frame(maxHeight: .infinity)
                } else if let errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                        .padding()
                } else if results.isEmpty {
                    Text("Enter a query to search Printables and Thingiverse")
                        .foregroundStyle(.secondary)
                        .frame(maxHeight: .infinity)
                } else {
                    List(results) { item in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title).font(.headline)
                            if let author = item.author {
                                Text(author).font(.caption).foregroundStyle(.secondary)
                            }
                            Text(item.sourceLabel).font(.caption2)
                        }
                    }
                }
            }
            .navigationTitle("GrabHub")
        }
    }

    private func performSearch() {
        isLoading = true
        errorMessage = nil
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                let result = searchFacade.searchBlocking(query: query)
                let mapped = result.items.map { ModelItemWrapper(from: $0) }
                DispatchQueue.main.async {
                    results = mapped
                    isLoading = false
                }
            } catch {
                DispatchQueue.main.async {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }
}

struct ModelItemWrapper: Identifiable {
    let id: String
    let title: String
    let author: String?
    let sourceLabel: String

    init(from item: ModelItem) {
        id = item.id
        title = item.title
        author = item.author
        sourceLabel = item.source.name
    }
}

#Preview {
    ContentView()
}
