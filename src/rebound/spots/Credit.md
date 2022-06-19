Stripes is a Servlet+JSP MVC framework created by Tim Fennell at http://stripes.mc4j.org/ (now a dead link, but it's [here](https://github.com/StripesFramework/stripes) and [here](https://stripesframework.atlassian.net/wiki/spaces/STRIPES/overview) and [here](https://en.wikipedia.org/wiki/Stripes_(framework)))<br>
   (which is, itself, based on [Struts](https://en.wikipedia.org/wiki/Apache_Struts_2) XD )

Spots is another MVC framework, but one that's *much* simpler (and among other things, doesn't automatically repopulate forms when they are invalid).

Spots takes alot of ideas from Stripes, but definitely not everything.

<br>
<br>

Also (currently) included in Spots util is a couple of classes from the Apache Commons Fileupload project and the core JRE.
   + This is because I needed to change a couple of things to make it work for Spots (namely visibility)
   + An added benefit though, is that Spots has no external dependencies.
   + These might get deleted or moved away if it turns out Servlet API >= 3.0 has the container (eg, Tomcat) taking over the responsibility for this (which makes sense to me)  :>

I hope there aren't any licensing issues with this.

    —Gotta Love Open-Source! :D
