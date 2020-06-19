package ws.slink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandLineArguments {

    @Autowired
    public CommandLineArguments(ApplicationArguments args, AppConfig appConfig) {
        if (args.containsOption("all")) {
            appConfig.offsetStr("*");
        }
        if (args.containsOption("since")) {
            appConfig.offsetStr(args.getOptionValues("since").get(0));
        }
        if (args.containsOption("dry-run")) {
            appConfig.dryRun(true);
        }
        if (args.containsOption("url")) {
            appConfig.url(args.getOptionValues("url").get(0));
        }
        if (args.containsOption("user")) {
            appConfig.user(args.getOptionValues("user").get(0));
        }
        if (args.containsOption("token")) {
            appConfig.token(args.getOptionValues("token").get(0));
        }
    }

}
