package com.rafalj.copyrightsgenerator.engine;

import com.opencsv.CSVWriter;
import com.rafalj.copyrightsgenerator.model.GitSearchResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitFinder {

    private static final String LINK_PREFIX = "/";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String regexPattern = "\\b[A-Z]+-\\d+\\b"; // Matches letters followed by hyphen and numbers

    // Compile the pattern
    private Pattern pattern = Pattern.compile(regexPattern);


    public List<GitSearchResult> generateReports(String value, LocalDate localFromDate, LocalDate localToDate) throws IOException, GitAPIException, ParseException {
        try(
            Git git = Git.open(new File(value + "/.git"));
            Repository repository = git.getRepository()
        ) {

            // Retrieve the current user's name from the Git configuration
            Config config = repository.getConfig();
            String currentUser = config.getString("user", null, "name");
            Date fromDate = getDate(localFromDate);
            Date toDate = getDate(localToDate);

            List<GitSearchResult> gitSearchResults = new ArrayList<>();

            // Iterate through the commits and print their details if the author matches the current user
            for (RevCommit commit : git.log().all().call()) {
                // Check if the commit's author matches the current user and the commit date falls within the specified range
                if (commit.getAuthorIdent().getName().equals(currentUser) &&
                        (fromDate == null || commit.getAuthorIdent().getWhen().after(fromDate)) &&
                        (toDate == null || commit.getAuthorIdent().getWhen().before(toDate))) {
                    gitSearchResults.add(GitSearchResult.builder()
                        .id(commit.getName())
                        .user(commit.getAuthorIdent().getName())
                        .message(commit.getFullMessage())
                        .date(commit.getAuthorIdent().getWhen())
                        .build());
                }
            }
            return gitSearchResults;
        }
    }

    private static Date getDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public void saveReport(String repositoryPath, String patchDirectory, List<GitSearchResult> results) throws IOException, GitAPIException {
        createCSVFile(patchDirectory, results);
        createPatchFiles(repositoryPath, patchDirectory, results);
    }

    private void createCSVFile(String patchDirectory, List<GitSearchResult> results) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(Paths.get(patchDirectory, "report.csv").toAbsolutePath().toString()))) {
            String[] header = {"Date", "Title", "Project code", "Link"};
            csvWriter.writeNext(header);

            for (GitSearchResult result : results) {
                String projectCode = extractProjectCode(result.getMessage());
                String[] data = {sdf.format(result.getDate()),
                        result.getMessage().trim(),
                        projectCode,
                        LINK_PREFIX + projectCode};
                csvWriter.writeNext(data);
            }
        }
    }

    private String extractProjectCode(String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group() : "";
    }

    private static void createPatchFiles(String repositoryPath, String patchDirectory, List<GitSearchResult> results) throws IOException, GitAPIException {
        try (
                Git git = Git.open(new File(repositoryPath + "/.git"));
                Repository repository = git.getRepository();
                RevWalk revWalk = new RevWalk(repository)
        ) {
            for (GitSearchResult result : results) {
                // Resolve the commit ID
                ObjectId commitObjectId = repository.resolve(result.getId());
                RevCommit commit = revWalk.parseCommit(commitObjectId);

                // Get parent commit for diff
                RevCommit parentCommit = revWalk.parseCommit(commit.getParent(0).getId());
                // Create file for patch
                String patchFileName = "patch_" + result.getMessage().trim() + ".patch";
                File patchFile = Paths.get(patchDirectory, patchFileName).toFile();
                patchFile.createNewFile();

                try (FileOutputStream fos = new FileOutputStream(patchFile);
                    DiffFormatter diffFormatter = new DiffFormatter(fos)) {
                    diffFormatter.setRepository(repository);
                    diffFormatter.setContext(0);

                    // Get the tree of the parent commit
                    try (ObjectReader reader = repository.newObjectReader()) {
                        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                        oldTreeIter.reset(reader, parentCommit.getTree().getId());

                        // Get the tree of the current commit
                        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                        newTreeIter.reset(reader, commit.getTree().getId());

                        // Get the list of changes as patches
                        List<DiffEntry> diffs = git.diff()
                                .setOldTree(oldTreeIter)
                                .setNewTree(newTreeIter)
                                .call();

                        // Write patches to file
                        for (DiffEntry diff : diffs) {
                            diffFormatter.format(diff);
                            diffFormatter.flush();
                        }
                    }
                }
            }
        }
    }
}
