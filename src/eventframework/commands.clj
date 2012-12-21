(ns eventframework.commands)

(defn new-uuid [] (str (java.util.UUID/randomUUID)))

(def initial-position "0")

(defn starting-state []
  (assoc {:uuids #{} :commands [] :waiting []}
    :uuid (new-uuid)))

(defn to-position [s i]
  (if (= i 0)
    initial-position
    (str (:uuid s) ":" i)))

(defn from-position [s p]
  (let [rm (re-matches #"([a-z0-9-]+):([0-9]+)" p)]
    (cond
     (= p initial-position) 0
     (nil? rm) nil
     (not= (rm 1) (:uuid s)) nil
     :else (let [i (Integer/parseInt (rm 2))]
             (if (> i (count (:commands s))) nil i)))))

(def ^:dynamic command-state (ref (starting-state)))

(defmacro with-clear-commands [& body]
  `(binding [command-state (ref (starting-state))]
    (do ~@body)))

(defn is-valid-position [p]
  (not (nil? (from-position (deref command-state) p))))

(defn get-commands-from [position]
  (let [s  (deref command-state)
        cl (:commands s)]
    [(to-position s (count cl)) (subvec cl (from-position s position))]))

(defn get-commands-to [position]
  (let [s  (deref command-state)
        cl (:commands s)]
    (subvec cl 0 (from-position s position))))

(defn append-command-get-waiting [uuid command]
  (dosync
   (let [s     (deref command-state)
         newcl (conj (:commands s) command)]
     (if (contains? (:uuids s) uuid)
       [(to-position s (count (:commands s))) nil]
       (do
         (ref-set command-state
                  (assoc s
                         :uuids    (conj (:uuids s) uuid) 
                         :commands newcl
                         :waiting  []))
         [(to-position s (count newcl))
          (:waiting s)])))))

(defn put-command [uuid command] 
  (let [[position toalert] (append-command-get-waiting uuid command)]
    (doseq [listener toalert]
      (listener position [command]))))
      
(defn get-after-or-add-waiting [position listener]
  (dosync
   (let [s  (deref command-state)
         cl (:commands s)
         ix (from-position s position)]
     (if (< ix (count cl))
       [(to-position s (count cl))
        (subvec cl ix)]
       (do 
         (ref-set command-state (update-in s [:waiting] #(conj % listener)))
         nil)))))

(defn listen-commands [position listener]
  (let [res (get-after-or-add-waiting position listener)]
    (if res (apply listener res))))
