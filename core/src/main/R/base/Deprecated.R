#  File src/library/base/R/Deprecated.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

###----- NOTE:	../man/base-deprecated.Rd   must be synchronized with this file!
###		-------------------------
.Deprecated <- function(new, package = NULL, msg) {
    msg <- if( missing(msg) ) {
	msg <- gettextf("'%s' is deprecated.\n",
			as.character(sys.call(sys.parent()))[1L] )
	if(!missing(new))
	    msg <- c(msg, gettextf("Use '%s' instead.\n", new))
	c(msg,
	  if(!is.null(package))
	  gettextf("See help(\"Deprecated\") and help(\"%s-deprecated\").",
		   package)
	  else gettext("See help(\"Deprecated\")"))
    }
    else as.character(msg)
    warning(paste(msg, collapse=""), call. = FALSE, domain = NA)
}

## consider keeping one (commented) entry here, for easier additions

## <entry>
## Deprecated in 2.5.0
Sys.putenv <- function(...) {
    .Deprecated("Sys.setenv")
    Sys.setenv(...)
}
## </entry>


## <entry>
## Deprecated in 2.14.0
.readRDS <- function(...) {
    .Deprecated("readRDS")
    readRDS(...)
}
.saveRDS <- function(...) {
    .Deprecated("saveRDS")
    saveRDS(...)
}
## </entry>

## Allow for future deprecation/defunct
.find.package <- function(...) find.package(...)
.path.package <- function(...) path.package(...)
