(defproject medicine "0.1.0-SNAPSHOT"
  :description "The API for patient database"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [org.clojure/data.json "2.4.0"]
                 [metosin/reitit-ring "0.5.17"]
                 [metosin/muuntaja "0.6.8"]
                 [ring/ring-core "1.9.0"]
                 [ring/ring-jetty-adapter "1.9.0"]
                 [com.github.seancorfield/next.jdbc "1.2.772"]
                 [org.postgresql/postgresql "42.2.10"]
                 [org.clojure/test.check "1.1.1"]
                 [metosin/reitit "0.5.17"]]
  
  :main ^:skip-aot medicine.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
