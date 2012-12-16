(ns eventframework.commands)

(defn from-position [p] (Integer/parseInt p))

(defn to-position [i] (Integer/toString i))

(def initial-position (to-position 0))

(def starting-state {:commands [] :waiting []})

(def ^:dynamic command-state (ref starting-state))

(defmacro with-clear-commands [& body]
  `(binding [command-state (ref starting-state)]
    (do ~@body)))

(defn get-commands [position] 
  (let [cl (:commands (deref command-state))]
    [(to-position (count cl)) (subvec cl (from-position position))]))

(defn append-commands-get-waiting [commandlist]
  (dosync (let [s (deref command-state) newcl (into (:commands s) commandlist)]
    (ref-set command-state {:commands newcl :waiting []})
    [(to-position (count newcl)) (:waiting s)])))

(defn put-commands [commandlist] 
  (let [[position toalert] (append-commands-get-waiting commandlist)]
    (doseq [listener toalert]
      (listener position commandlist))))
    
(defn put-command [command]
  (put-commands [command]))
  
(defn get-after-or-add-waiting [position listener]
  (dosync (let [s (deref command-state) cl (:commands s) ix (from-position position)]
    (if (< ix (count cl))
      [(to-position (count cl)) (subvec cl ix)]
      (do 
        (ref-set command-state {
          :commands cl :waiting (conj (:waiting s) listener)}) 
        nil)))))

(defn listen-commands [position listener]
  (let [res (get-after-or-add-waiting position listener)]
    (if res (listener (res 0) (res 1)))))
