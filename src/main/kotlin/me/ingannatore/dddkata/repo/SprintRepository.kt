package me.ingannatore.dddkata.repo

import me.ingannatore.dddkata.entity.Sprint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SprintRepository : JpaRepository<Sprint, Long>
