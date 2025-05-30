package ghutil

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int

class Command: CliktCommand(name = "ghsearch") {
    val terms: List<String> by argument()
        .help("Terms to search for")
        .multiple()

    val languages: List<String> by option("-l", "--language")
        .help("Primary language. Can be used multiple times.")
        .multiple()

    val sort: String? by option("-s", "--sort")
        .help("Specify sort option.")
        .choice("stars", "forks", "help-wanted-issues", "updated")

    val order: String? by option("-o", "--sortOrder")
        .help("Specify sort order.")
        .choice("asc", "desc")

    val stars by option("--stars")
        .help("""Constrain search based on stars: operator ',' count (example: '>=,200'). 
            |Make sure you quote the option value.""".trimMargin())
        .split(",")

    val limit: Int? by option("--limit")
        .help("Limit the search to x repositories")
        .int()

    val configFile: String? by option("--config")
        .help("Configuration file that will be overridden by command line options")

    override fun run() {
        var config = Config()
        println("configFile: ${this.configFile}")
        configFile?.let { config = loadConfig(it) }
        // TODO pass this to avoid naming all options
        config = overrideConfig(config, this)
        println("""running, terms: ${config.terms}, languages: ${config.languages}, sort: ${config.sort}, 
            |sortOrder: ${config.order}""".trimMargin())
        val repositories = searchPublicRepos(config)
        var item = 1
        run {
            repositories.forEach { repo ->
                println("\n\nRepository: ${repo.name} by ${repo.owner.login}/${repo.owner.name}/${repo.owner.company}")
                println("Description: ${repo.description}")
                println("Stars: ${repo.stargazersCount}, URL: ${repo.htmlUrl}")
                println("Created: ${repo.createdAt} / Updated: ${repo.updatedAt}")
                if (limit != null && item++ == limit!!) return@run
            }
        }
    }
}

fun main(args: Array<String>) = Command().main(args)
