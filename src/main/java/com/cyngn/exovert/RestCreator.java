package com.cyngn.exovert;

import com.cyngn.exovert.generate.server.rest.RestClientGenerator;
import com.cyngn.exovert.generate.server.rest.RestServerGenerator;
import com.cyngn.exovert.generate.server.rest.TypeGenerator;
import com.cyngn.vertx.async.Action;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Runs the  generation for
 * Server and Clients
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/16/15.
 */
public class RestCreator {

    // command line params
    private OptionSpec preview;
    private OptionSpec server;
    private OptionSpec client;
    private OptionSpec types;

    private OptionSpec<String> out;
    private OptionSpec<String> spec;
    private OptionSpec help;

    private OptionParser parser;

    private Map<OptionSpec<?>, Action> actions;

    private OptionSet optionSet;

    private RestCreator() throws Exception {
        parser = getParser();

        actions = new HashMap<>();
        actions.put(server, this::createServer);
        actions.put(client, this::createClient);
        actions.put(types, this::createTypes);
    }

    private void createServer() {
        try {
            RestServerGenerator.Builder builder = RestServerGenerator.newBuilder();
            if (optionSet.has(preview)) {
                builder.withIsPreview(true);
            } else {
                if (optionSet.has(out)) {
                    builder.withOutputDirectory(out.value(optionSet));
                }
            }

            if(optionSet.has(spec)) {
                builder.withSpecFilePath(spec.value(optionSet));
            }
            builder.build().generate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createClient() {
        try {
            RestClientGenerator.Builder builder = RestClientGenerator.newBuilder();
            if (optionSet.has(preview)) {
                builder.withIsPreview(true);
            } else if (optionSet.has(out)) {
                builder.withOutputDirectory(out.value(optionSet));
            }
            if(optionSet.has(spec)) {
                builder.withSpecFilePath(spec.value(optionSet));
            }

            builder.build().generate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTypes() {
        try {
            TypeGenerator.Builder builder = TypeGenerator.newBuilder();
            if (optionSet.has(preview)) {
                builder.withIsPreview(true);
            } else if (optionSet.has(out)) {
                builder.withOutputDirectory(out.value(optionSet));
            }

            if(optionSet.has(spec)) {
                builder.withSpecFilePath(spec.value(optionSet));
            }

            builder.build().generate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle parsing the command line args and directing the app path
     */
    private void run(String [] args) throws IOException {
        optionSet = parser.parse(args);

        OptionSpec<?> invalidSelection = null;
        Map.Entry<OptionSpec<?>, Action> spec = null;

        // figure out which primary action they selected and make sure they didn't choose two of them
        for (Map.Entry<OptionSpec<?>, Action> entry : actions.entrySet()) {
            if (optionSet.has(entry.getKey())) {
                if (spec != null) {
                    invalidSelection = entry.getKey();
                    break;
                }
                spec = entry;
            }
        }

        if (invalidSelection != null) {
            System.out.println("Competing options selected - option1: " + spec.getKey() + " option2: " +
                    invalidSelection + "\n");
            parser.printHelpOn(System.out);
            System.exit(-1);
        } else if (spec != null) {
            final Action handler = spec.getValue();
            handler.callback();
            System.exit(0);
        } else {
            parser.printHelpOn(System.out);
            System.exit(0);
        }
    }

    /**
     * Builds the command line parser
     */
    private OptionParser getParser() {
        OptionParser parser = new OptionParser();
        server = parser.acceptsAll(asList("server"), "create the server files on disk");
        client = parser.acceptsAll(asList("client"), "create the client files on disk");
        types = parser.acceptsAll(asList("types"), "create the type files on disk");

        preview = parser.acceptsAll(asList("preview", "p"), "output all the java files to the console, don't create files");

        out = parser.acceptsAll(asList("out", "o"), "the output dir in which to place files")
                .withRequiredArg()
                .defaultsTo("build/generated-src")
                .ofType(String.class);

        spec = parser.acceptsAll(asList("spec", "f"), "specification file")
                .withRequiredArg()
                .defaultsTo("api.json")
                .ofType(String.class);

        help = parser.accepts("help", "shows this message").forHelp();
        return parser;
    }

    /**
     * Entry point.
     * @param args command-line args
     * @throws Exception runtime errors
     */
    public static void main(String [] args) throws Exception {
        new RestCreator().run(args);
    }

}
