package me.ingannatore.dddkata.repo

import me.ingannatore.dddkata.entity.Release
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReleaseRepository : JpaRepository<Release, Long>
