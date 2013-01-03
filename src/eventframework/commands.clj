(ns eventframework.commands)

(defrecord Message
    [id type body])

(defn new-uuid [] (str (java.util.UUID/randomUUID)))

(def initial-position "0")

(defn starting-state []
  {:uuids #{} :commands [] :waiting [] :uuid (new-uuid)})

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

;; FIXME(alexander): make sure this has right thread-scope
(def command-state (ref (starting-state)))

(defn is-valid-position [p]
  (not (nil? (from-position (deref command-state) p))))


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

(defn get-commands-before [position]
  (let [s  (deref command-state)
        cl (:commands s)
        ix (from-position s position)]
    (subvec cl 0 ix)))

(defn get-latest-position-and-commands-after [state position]
  (let [cl (:commands state)
        ix (from-position state position)]
    (when (< ix (count cl))
      [(to-position state (count cl))
       (subvec cl ix)])))

;; FIXME(alexander): clean this up once more
(defn- get-after-or-add-waiting! [position listener]
  (dosync
   (let [state (deref command-state)]
     (or (get-latest-position-and-commands-after state position)
         (do (ref-set command-state
                      (update-in state [:waiting] #(conj % listener)))
             nil)))))

(defn apply-or-enqueue-listener! [position listener]
  "Apply `listener` to all commands past `position`, or if none, enqueue it."
  (when-let [[new-pos new-commands] (get-after-or-add-waiting! position listener)]
    (listener new-pos new-commands)
    nil))
