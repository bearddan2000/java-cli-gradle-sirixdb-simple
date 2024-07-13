package example;

import org.sirix.access.DatabaseConfiguration;
import org.sirix.access.Databases;
import org.sirix.access.ResourceConfiguration;
import org.sirix.axis.DescendantAxis;
import org.sirix.axis.IncludeSelf;
import org.sirix.axis.filter.FilterAxis;
import org.sirix.axis.filter.json.JsonNameFilter;
import org.sirix.service.json.shredder.JsonShredder;
import org.sirix.settings.VersioningType;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static Path JSON_DIRECTORY = Paths.get("src", "main", "java", "resources");

    private static Path databaseFile = Paths.get("src", "main", "java", "resources", "database");

    private static void writeJson()throws IOException {
        final var pathToJsonFile = JSON_DIRECTORY.resolve("dog.json");

        // Create an empty JSON database.
        Databases.createJsonDatabase(new DatabaseConfiguration(databaseFile));

        // Open the database.
        try (final var database = Databases.openJsonDatabase(databaseFile)) {
            // Create a resource to store a JSON-document.
            database.createResource(ResourceConfiguration.newBuilder("resource")
                    .useTextCompression(false)
                    .useDeweyIDs(true)
                    .versioningApproach(VersioningType.DIFFERENTIAL)
                    .revisionsToRestore(3)
                    .buildPathSummary(true)
                    .build());

            // Import JSON.
            try (final var manager = database.openResourceManager("resource");
                 final var wtx = manager.beginNodeTrx()) {
                wtx.insertSubtreeAsFirstChild(JsonShredder.createFileReader(pathToJsonFile));
                wtx.commit();
            }
        }
    }

    private static void readJson() {

        // Open the database.
        try (final var database = Databases.openJsonDatabase(databaseFile)) {
            // Create a resource to store a JSON-document.
            database.createResource(ResourceConfiguration.newBuilder("resource")
                    .useTextCompression(false)
                    .useDeweyIDs(true)
                    .versioningApproach(VersioningType.DIFFERENTIAL)
                    .revisionsToRestore(3)
                    .buildPathSummary(true)
                    .build());
            System.out.println("database created");

            // Import JSON.
            try (final var manager = database.openResourceManager("resource");
                 final var wtx = manager.beginNodeTrx()) {
                wtx.insertSubtreeAsFirstChild(JsonShredder.createFileReader(JSON_DIRECTORY));
                wtx.commit();
                wtx.moveToDocumentRoot();

                int foundTimes = 0;

                final var axis = new DescendantAxis(wtx, IncludeSelf.YES);
                final var filter = new JsonNameFilter(wtx, "White");

                for (var filterAxis = new FilterAxis<>(axis, filter); filterAxis.hasNext();) {
                    filterAxis.next();
                    foundTimes++;
                }
                System.out.println("filter found: "+foundTimes);
            }
        }
    }

	public static void main(String[] args) {
        try{
            writeJson();
            readJson();
        } catch(Exception e){ 
            e.printStackTrace(); 
        }
	}
}
